package com.example.ledger.web;

import com.example.ledger.dto.PendingEventRequest;
import com.example.ledger.entity.PendingEvent;
import com.example.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

  private final LedgerService service;

  public LedgerController(LedgerService service) {
    this.service = service;
  }

  @GetMapping("/events")
  public List<PendingEvent> getEvents(
          @RequestHeader(value = "X-User-Id", required = false) String xUserId,
          @AuthenticationPrincipal Jwt jwt
  ) {
    long userId = resolveUserId(xUserId, jwt);
    return service.getEvents(userId);
  }

  @PostMapping("/events")
  public List<PendingEvent> addEvents(
          @RequestHeader(value = "X-User-Id", required = false) String xUserId,
          @AuthenticationPrincipal Jwt jwt,
          @Valid @RequestBody List<@Valid PendingEventRequest> events
  ) {
    long userId = resolveUserId(xUserId, jwt);
    return service.addEvents(userId, events);
  }

  /** Prefer X-User-Id added by the gateway, fallback to JWT sub. */
  private long resolveUserId(String xUserId, Jwt jwt) {
    String id = (xUserId != null && !xUserId.isBlank())
            ? xUserId
            : (jwt != null ? jwt.getSubject() : null);

    if (id == null || id.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing user identity");
    }
    try {
      return Long.parseLong(id);
    } catch (NumberFormatException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id must be numeric");
    }
  }
}
