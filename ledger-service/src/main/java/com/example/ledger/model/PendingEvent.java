package com.example.ledger.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class PendingEvent {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long userId;
  private LocalDate date;
  private double amount;
  private String description;
  public PendingEvent() {}
  public PendingEvent(Long userId, LocalDate date, double amount, String description) {
    this.userId = userId; this.date = date; this.amount = amount; this.description = description;
  }
  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public LocalDate getDate() { return date; }
  public double getAmount() { return amount; }
  public String getDescription() { return description; }
  public void setId(Long id) { this.id = id; }
  public void setUserId(Long userId) { this.userId = userId; }
  public void setDate(LocalDate date) { this.date = date; }
  public void setAmount(double amount) { this.amount = amount; }
  public void setDescription(String description) { this.description = description; }
}
