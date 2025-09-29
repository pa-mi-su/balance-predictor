package com.example.ledger.web;

import com.example.ledger.model.PendingEvent;
import com.example.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {
  private final LedgerService svc;
  public LedgerController(LedgerService svc) { this.svc = svc; }

  @Operation(summary="List pending events",
      responses={@ApiResponse(responseCode="200",
      content=@Content(array=@ArraySchema(schema=@Schema(implementation=PendingEvent.class))))})
  @GetMapping("/events")
  public List<PendingEvent> list(@RequestParam @NotNull Long userId) { return svc.list(userId); }

  @Operation(summary="Add pending events")
  @PostMapping("/events")
  public List<PendingEvent> add(@RequestParam @NotNull Long userId, @RequestBody List<PendingEvent> events) {
    return svc.addAll(userId, events);
  }

  @Operation(summary="Quick add single event")
  @PostMapping("/event")
  public List<PendingEvent> addOne(@RequestParam @NotNull Long userId,
                                   @RequestParam double amount,
                                   @RequestParam String description,
                                   @RequestParam(required=false) String date) {
    PendingEvent e = new PendingEvent(userId, date==null? LocalDate.now(): LocalDate.parse(date), amount, description);
    return svc.addAll(userId, List.of(e));
  }

  @Operation(summary="Clear all pending events for user")
  @DeleteMapping("/events")
  public void clear(@RequestParam @NotNull Long userId) { svc.clear(userId); }
}
