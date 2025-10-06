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

import java.util.Map;

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

  /** Sandbox helper: get a public_token without Link UI. */
  @PostMapping(path = "/sandbox/public_token/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> sandboxPublicToken(@RequestBody LinkTokenCreateRequest req) {
    Assert.notNull(req.userId(), "userId required");

    var body = Map.of(
            "client_id", clientId,
            "secret", secret,
            "institution_id", "ins_109508",              // Plaid sandbox “Chase”
            "initial_products", new String[]{"transactions"}
    );

    return plaid.post()
            .uri("/sandbox/public_token/create")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(MAP_REF);
  }

  /** Exchange public_token -> access_token and stash it (temp in-memory). */
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
              if (accessToken != null) {
                tokenStore.put(req.userId(), accessToken);
              }
              return resp;
            });
  }

  /** Real balances from Plaid sandbox via /accounts/balance/get. */
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
}
