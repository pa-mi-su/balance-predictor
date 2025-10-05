package com.example.ledger.repo;

import com.example.ledger.entity.PendingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PendingEventRepository extends JpaRepository<PendingEvent, Long> {

  List<PendingEvent> findByUserIdOrderByDateAscIdAsc(Long userId);

  boolean existsByUserIdAndDateAndAmountAndDescription(
          Long userId, LocalDate date, BigDecimal amount, String description);
}
