package com.example.ledger.repo;

import com.example.ledger.model.PendingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PendingEventRepository extends JpaRepository<PendingEvent, Long> {
  List<PendingEvent> findByUserId(Long userId);
}
