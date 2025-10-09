package com.example.ledger.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "pending_events",
        indexes = {
                @Index(name = "idx_pending_events_user_date", columnList = "user_id,event_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_pending_events_natural",
                        columnNames = {"user_id","event_date","amount","description"}
                )
        }
)
public class PendingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="event_date", nullable=false)
    private LocalDate date;

    @Column(name="amount", nullable=false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name="description", nullable=false, length = 255)
    private String description;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters & setters

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public LocalDate getDate() { return date; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
