package com.example.plaid.web;

import com.example.plaid.config.TokenStore;
import com.example.plaid.dto.LinkTokenCreateRequest;
import com.example.plaid.dto.PublicTokenExchangeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
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

  // ---------- Helpers ----------

  /** Prefer X-User-Id (gateway), fallback to JWT sub. Require numeric id. */
  private long resolveUserId(String xUserId, Jwt jwt) {
    String id = StringUtils.hasText(xUserId) ? xUserId : (jwt != null ? jwt.getSubject() : null);
    if (!StringUtils.hasText(id)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing user identity");
    }
    try {
      return Long.parseLong(id);
    } catch (NumberFormatException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id must be numeric");
    }
  }

  // ---------- Endpoints ----------

  /** Create a Plaid Link token (front-end usually uses this). */
  @PostMapping(path = "/link/token/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> createLinkToken(
          @RequestHeader(value = "X-User-Id", required = false) String xUserId,
          @AuthenticationPrincipal Jwt jwt,
          @RequestBody(required = false) LinkTokenCreateRequest ignored // kept for compatibility; userId ignored
  ) {
    long userId = resolveUserId(xUserId, jwt);

    var body = Map.of(
            "client_id", clientId,
            "secret", secret,
            "client_name", "Balance Predictor",
            "country_codes", new String[] {"US"},
            "language", "en",
            "products", new String[] {"transactions"},
            "user", Map.of("client_user_id", String.valueOf(userId)),
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
  public Mono<Map<String, Object>> sandboxPublicToken(
          @RequestHeader(value = "X-User-Id", required = false) String xUserId,
          @AuthenticationPrincipal Jwt jwt,
          @RequestBody(required = false) LinkTokenCreateRequest ignored // userId ignored
  ) {
    long userId = resolveUserId(xUserId, jwt);

    var body = Map.of(
            "client_id", clientId,
            "secret", secret,
            "institution_id", "ins_109508", // Plaid sandbox "First Platypus Bank"
            "initial_products", new String[] {"transactions"}
    );

    // Note: /sandbox/public_token/create does not take client_user_id; association happens on exchange
    return plaid.post()
            .uri("/sandbox/public_token/create")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(MAP_REF);
  }

  /** Exchange public_token -> access_token and persist it for the resolved user. */
  @PostMapping(path = "/item/public_token/exchange", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> exchangePublicToken(
          @RequestHeader(value = "X-User-Id", required = false) String xUserId,
          @AuthenticationPrincipal Jwt jwt,
          @RequestBody PublicTokenExchangeRequest req
  ) {
    long userId = resolveUserId(xUserId, jwt);
    if (!StringUtils.hasText(req.publicToken())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "publicToken required");
    }

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
                tokenStore.put(userId, accessToken, itemId); // associate with resolved user
              }
              return resp;
            });
  }

  /** Real balances from Plaid sandbox via /accounts/balance/get. */
  @GetMapping(path = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, Object>> getBalance(
          @RequestHeader(value = "X-User-Id", required = false) String xUserId,
          @AuthenticationPrincipal Jwt jwt
  ) {
    long userId = resolveUserId(xUserId, jwt);

    var accessToken = tokenStore.get(userId);
    if (accessToken == null) {
      return Mono.just(Map.of(
              "error", "NO_ACCESS_TOKEN",
              "message", "Exchange a public_token first for this user."
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
