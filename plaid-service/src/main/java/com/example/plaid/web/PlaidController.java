package com.example.plaid.web;

import com.example.plaid.model.AccountBalance;
import com.example.plaid.service.MockPlaidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plaid")
public class PlaidController {
  private final MockPlaidService plaid;
  public PlaidController(MockPlaidService plaid) { this.plaid = plaid; }

  @Operation(summary="Get current balance (mock)",
      responses={@ApiResponse(responseCode="200",
      content=@Content(schema=@Schema(implementation=AccountBalance.class)))})
  @GetMapping("/balance")
  public AccountBalance getBalance(@RequestParam Long userId) { return plaid.getCurrentBalance(userId); }

  @Operation(summary="Set mock base balance")
  @PostMapping("/balance")
  public void setBalance(@RequestParam double amount) { plaid.setMockBalance(amount); }
}
