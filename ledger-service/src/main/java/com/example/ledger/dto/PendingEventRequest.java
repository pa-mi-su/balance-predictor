package com.example.ledger.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PendingEventRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    private BigDecimal amount;

    @NotNull
    @Size(min = 1, max = 255)
    private String description;

    public LocalDate getDate() { return date; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }

    public void setDate(LocalDate date) { this.date = date; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
}
