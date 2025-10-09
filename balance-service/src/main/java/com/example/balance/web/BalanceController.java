package com.example.balance.web;

import com.example.balance.model.Projection;
import com.example.balance.service.BalanceCalcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {

  private final BalanceCalcService svc;

  public BalanceController(BalanceCalcService svc) {
    this.svc = svc;
  }

  @Operation(
          summary = "Projected balance (current + pending)",
          description = """
        Identity is taken from the authenticated request.
        - Preferred: `X-User-Id` header added by the API Gateway after JWT validation.
        - Fallback: JWT subject (`sub`) if header is missing.
        """,
          parameters = {
                  @Parameter(name = "X-User-Id", description = "Injected by gateway after auth", required = false)
          },
          responses = {
                  @ApiResponse(responseCode = "200",
                          content = @Content(schema = @Schema(implementation = Projection.class))),
                  @ApiResponse(responseCode = "400", description = "Invalid or non-numeric user id"),
                  @ApiResponse(responseCode = "401", description = "Missing authentication / user id")
          }
  )
  @GetMapping("/running")
  public Mono<Projection> running(
          @RequestHeader(value = "X-User-Id", required = false) String xUserId,
          @AuthenticationPrincipal Jwt jwt
  ) {
    String userIdStr = (xUserId != null && !xUserId.isBlank())
            ? xUserId
            : (jwt != null ? jwt.getSubject() : null);

    if (userIdStr == null || userIdStr.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing user identity");
    }

    final long userId;
    try {
      userId = Long.parseLong(userIdStr);
    } catch (NumberFormatException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id must be numeric");
    }

    return svc.projectedBalance(userId).map(Projection::new);
  }
}
