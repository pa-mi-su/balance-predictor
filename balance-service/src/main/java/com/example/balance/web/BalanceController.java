package com.example.balance.web;

import com.example.balance.model.Projection;
import com.example.balance.service.BalanceCalcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;
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
          description = "Uses Plaid for the current balance and Ledger events for pending deltas. "
                  + "If accountId is provided, only that Plaid account is used; otherwise a default/primary account is used.",
          responses = {
                  @ApiResponse(responseCode = "200",
                          content = @Content(schema = @Schema(implementation = Projection.class)))
          }
  )
  @GetMapping("/running")
  public Mono<Projection> running(
          @Parameter(description = "Internal user id") @RequestParam Long userId,
          @Parameter(description = "Optional Plaid account id to scope the calculation")
          @RequestParam(name = "accountId", required = false) String accountId
  ) {
    Mono<Double> calc = (accountId == null || accountId.isBlank())
            ? svc.projectedBalance(userId)
            : svc.projectedBalanceForAccount(userId, accountId);

    return calc.map(Projection::new);
  }
}
