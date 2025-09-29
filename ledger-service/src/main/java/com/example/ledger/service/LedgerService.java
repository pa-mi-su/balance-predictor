package com.example.ledger.service;

import com.example.ledger.model.PendingEvent;
import com.example.ledger.repo.PendingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class LedgerService {
  private final PendingEventRepository repo;
  public LedgerService(PendingEventRepository repo) { this.repo = repo; }
  public List<PendingEvent> list(Long userId) { return repo.findByUserId(userId); }
  @Transactional
  public List<PendingEvent> addAll(Long userId, List<PendingEvent> events) {
    for (PendingEvent e : events) { if (e.getUserId() == null) e.setUserId(userId); repo.save(e); }
    return list(userId);
  }
  @Transactional
  public void clear(Long userId) { for (PendingEvent e : list(userId)) repo.delete(e); }
}
