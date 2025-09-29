package com.example.balance.model;
import java.time.LocalDate;
public record PendingEvent(Long id, Long userId, LocalDate date, double amount, String description) {}
