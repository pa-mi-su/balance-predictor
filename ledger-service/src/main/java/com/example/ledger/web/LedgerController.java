package com.example.ledger.web;

import com.example.ledger.dto.PendingEventRequest;
import com.example.ledger.entity.PendingEvent;
import com.example.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

  private final LedgerService service;

  public LedgerController(LedgerService service) {
    this.service = service;
  }

  @GetMapping("/events")
  public List<PendingEvent> getEvents(@RequestParam Long userId) {
    return service.getEvents(userId);
  }

  @PostMapping("/events")
  public List<PendingEvent> addEvents(
          @RequestParam Long userId,
          @Valid @RequestBody List<@Valid PendingEventRequest> events) {
    return service.addEvents(userId, events);
  }
}
