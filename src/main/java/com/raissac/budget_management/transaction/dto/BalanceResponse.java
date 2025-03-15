package com.raissac.budget_management.transaction.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal balance
) {
}
