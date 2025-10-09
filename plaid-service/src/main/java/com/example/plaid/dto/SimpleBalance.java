package com.example.plaid.dto;

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
) {}
