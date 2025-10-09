package com.example.ledger.service;

import com.example.ledger.dto.PendingEventRequest;
import com.example.ledger.entity.PendingEvent;
import com.example.ledger.repo.PendingEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LedgerService {

  private final PendingEventRepository repo;

  public LedgerService(PendingEventRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public List<PendingEvent> addEvents(Long userId, List<PendingEventRequest> requests) {
    for (var r : requests) {
      // App-level dedupe to avoid roundtrips/violations where possible
      boolean exists = repo.existsByUserIdAndDateAndAmountAndDescription(
              userId, r.getDate(), r.getAmount(), r.getDescription());
      if (exists) continue;

      var e = new PendingEvent();
      e.setUserId(userId);
      e.setDate(r.getDate());
      e.setAmount(r.getAmount());
      e.setDescription(r.getDescription());

      try {
        repo.save(e);
      } catch (DataIntegrityViolationException ignored) {
        // Unique index is the final guard (another request may have inserted same record)
      }
    }
    return repo.findByUserIdOrderByDateAscIdAsc(userId);
  }

  @Transactional(readOnly = true)
  public List<PendingEvent> getEvents(Long userId) {
    return repo.findByUserIdOrderByDateAscIdAsc(userId);
  }
}
