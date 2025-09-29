package com.example.balance.web;

import com.example.balance.model.Projection;
import com.example.balance.service.BalanceCalcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {
  private final BalanceCalcService svc;
  public BalanceController(BalanceCalcService svc) { this.svc = svc; }

  @Operation(summary="Projected balance (current + pending)",
      responses={@ApiResponse(responseCode="200",
      content=@Content(schema=@Schema(implementation=Projection.class)))})
  @GetMapping("/running")
  public Mono<Projection> running(@RequestParam Long userId) {
    return svc.projectedBalance(userId).map(Projection::new);
  }
}
