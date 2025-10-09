package com.example.plaid.web;

import com.example.plaid.config.TokenStore;
import com.example.plaid.dto.LinkTokenCreateRequest;
import com.example.plaid.dto.PublicTokenExchangeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Predicate;

@RestController
@RequestMapping("/api/plaid")
public class PlaidController {

  private final WebClient plaid;
  private final TokenStore tokenStore;

  @Value("${plaid.client-id}")
  private String clientId;

  @Value("${plaid.secret}")
  private String secret;

  @Value("${plaid.webhook-url:}")
  private String webhookUrl;

  public PlaidController(WebClient plaidWebClient, TokenStore tokenStore) {
    this.plaid = plaidWebClient;
    this.tokenStore = tokenStore;
  }

  private static final ParameterizedTypeReference<Map<String, Object>> MAP_REF =
          new ParameterizedTypeReference<>() {};

  // =====================================================
  // LINK + SANDBOX + TOKEN EXCHANGE
  // =====================================================

  @PostMapping(path = "/link/token/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> createLinkToken(@RequestBody LinkTokenCreateRequest req) {
    Assert.notNull(req.userId(), "userId required");

    var body = Map.of(
            "client_id", clientId,
            "secret", secret,
            "client_name", "Balance Predictor",
            "country_codes", new String[]{"US"},
            "language", "en",
            "products", new String[]{"transactions"},
            "user", Map.of("client_user_id", String.valueOf(req.userId())),
            "webhook", webhookUrl == null ? "" : webhookUrl
    );

    return plaid.post()
            .uri("/link/token/create")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(MAP_REF);
  }

  @PostMapping(path = "/sandbox/public_token/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> sandboxPublicToken(@RequestBody LinkTokenCreateRequest req) {
    Assert.notNull(req.userId(), "userId required");

    var body = Map.of(
            "client_id", clientId,
            "secret", secret,
            "institution_id", "ins_109508",
            "initial_products", new String[]{"transactions"}
    );

    return plaid.post()
            .uri("/sandbox/public_token/create")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(MAP_REF);
  }

  @PostMapping(path = "/item/public_token/exchange", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> exchangePublicToken(@RequestBody PublicTokenExchangeRequest req) {
    Assert.notNull(req.userId(), "userId required");
    Assert.hasText(req.publicToken(), "publicToken required");

    var body = Map.of(
            "client_id", clientId,
            "secret", secret,
            "public_token", req.publicToken()
    );

    return plaid.post()
            .uri("/item/public_token/exchange")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(MAP_REF)
            .map(resp -> {
              var accessToken = (String) resp.get("access_token");
              var itemId      = (String) resp.get("item_id");
              if (accessToken != null && itemId != null) {
                tokenStore.put(req.userId(), accessToken, itemId);
              }
              return resp;
            });
  }

  // =====================================================
  // BALANCE ENDPOINTS
  // =====================================================

  /** Raw Plaid balances (unchanged). */
  @GetMapping(path = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> getBalance(@RequestParam Long userId) {
    var accessToken = tokenStore.get(userId);
    if (accessToken == null) {
      return Mono.just(Map.of(
              "error", "NO_ACCESS_TOKEN",
              "message", "Exchange a public_token first for this userId."
      ));
    }

    var body = Map.of(
            "client_id", clientId,
            "secret", secret,
            "access_token", accessToken
    );

    return plaid.post()
            .uri("/accounts/balance/get")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(MAP_REF);
  }

  // --- NEW: balance for a specific Plaid account_id ---
  @GetMapping(path = "/balance/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> getBalanceForAccount(
          @RequestParam Long userId,
          @PathVariable String accountId) {

    var accessToken = tokenStore.get(userId);
    if (accessToken == null) {
      return Mono.just(Map.of(
              "error", "NO_ACCESS_TOKEN",
              "message", "Exchange a public_token first for this userId."
      ));
    }

    var body = Map.of(
            "client_id", clientId,
            "secret", secret,
            "access_token", accessToken
    );

    return plaid.post()
            .uri("/accounts/balance/get")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(MAP_REF)
            .map(resp -> {
              @SuppressWarnings("unchecked")
              var accounts = (java.util.List<java.util.Map<String, Object>>) resp.getOrDefault("accounts", java.util.List.of());
              var match = accounts.stream()
                      .filter(a -> accountId.equals(a.get("account_id")))
                      .findFirst()
                      .orElse(null);

              if (match == null) {
                return Map.of(
                        "error", "ACCOUNT_NOT_FOUND",
                        "accountId", accountId
                );
              }

              @SuppressWarnings("unchecked")
              var balances = (java.util.Map<String, Object>) match.get("balances");
              var currency = balances.get("iso_currency_code") != null
                      ? balances.get("iso_currency_code")
                      : balances.get("unofficial_currency_code");

              return Map.of(
                      "balance", Map.of(
                              "account_id", match.get("account_id"),
                              "name", match.get("name"),
                              "mask", match.get("mask"),
                              "type", match.get("type"),
                              "subtype", match.get("subtype"),
                              "available", balances.get("available"),
                              "current", balances.get("current"),
                              "currency", currency
                      )
              );
            });
  }

  /** Clean version — pick one account (by id or best default). */
  @GetMapping(path = "/balance/primary", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> getPrimaryBalance(
          @RequestParam Long userId,
          @RequestParam(required = false) String accountId) {

    var accessToken = tokenStore.get(userId);
    if (accessToken == null) {
      return Mono.just(Map.of("error", "NO_ACCESS_TOKEN", "message", "Exchange a public_token first."));
    }

    var body = Map.of(
            "client_id", clientId,
            "secret", secret,
            "access_token", accessToken
    );

    return plaid.post()
            .uri("/accounts/balance/get")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(MAP_REF)
            .map(resp -> Map.of("balance", extractPrimaryBalance(resp, accountId)));
  }

  // =====================================================
  // HELPER METHODS
  // =====================================================

  @SuppressWarnings("unchecked")
  private Map<String, Object> extractPrimaryBalance(Map<String, Object> resp, String preferredId) {
    var accounts = (List<Map<String, Object>>) resp.getOrDefault("accounts", List.of());
    if (accounts.isEmpty()) return Map.of("error", "No accounts returned");

    Map<String, Object> chosen = null;

    if (preferredId != null) {
      chosen = accounts.stream()
              .filter(a -> preferredId.equals(a.get("account_id")))
              .findFirst()
              .orElse(null);
    }

    if (chosen == null) chosen = find(accounts, a -> "depository".equals(a.get("type")) && "checking".equals(a.get("subtype")));
    if (chosen == null) chosen = find(accounts, a -> "depository".equals(a.get("type")) && "savings".equals(a.get("subtype")));
    if (chosen == null) chosen = find(accounts, a -> {
      var b = (Map<String, Object>) a.get("balances");
      return b != null && (b.get("available") != null || b.get("current") != null);
    });
    if (chosen == null) chosen = accounts.get(0);

    var balances = (Map<String, Object>) chosen.get("balances");
    var available = balances.get("available");
    var current = balances.get("current");
    var currency = balances.get("iso_currency_code") != null
            ? balances.get("iso_currency_code")
            : balances.get("unofficial_currency_code");

    return Map.of(
            "account_id", chosen.get("account_id"),
            "name", chosen.get("name"),
            "mask", chosen.get("mask"),
            "type", chosen.get("type"),
            "subtype", chosen.get("subtype"),
            "available", available,
            "current", current,
            "currency", currency
    );
  }

  private static Map<String, Object> find(List<Map<String, Object>> list, Predicate<Map<String, Object>> predicate) {
    for (var a : list) if (predicate.test(a)) return a;
    return null;
  }
}
