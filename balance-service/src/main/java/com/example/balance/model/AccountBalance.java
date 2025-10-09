package com.example.balance.model;

import java.util.List;

/**
 * Represents a simplified Plaid balance response.
 * Can handle either a single-account response or multiple accounts.
 */
public record AccountBalance(
        List<AccountInfo> accounts
) {
    /**
     * Utility to extract the "current" balance.
     * If multiple accounts exist, takes the first one by default.
     */
    public double currentBalance() {
        if (accounts == null || accounts.isEmpty()) return 0.0;
        return accounts.get(0).balances().current();
    }

    /**
     * Inner record to represent a single account entry.
     */
    public record AccountInfo(
            String account_id,
            String name,
            Balances balances
    ) {}

    /**
     * Inner record to represent balances per account.
     */
    public record Balances(
            Double available,
            Double current,
            String iso_currency_code
    ) {}
}
