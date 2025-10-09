package com.example.balance.dto;

public record SimpleBalance(
        Long userId,
        String accountId,
        String name,
        String mask,
        String type,
        String subtype,
        Double available,
        Double current,
        String currency
) {
    public double base() {
        // prefer available for depository if present, else current
        return available != null ? available : (current != null ? current : 0d);
    }
}
