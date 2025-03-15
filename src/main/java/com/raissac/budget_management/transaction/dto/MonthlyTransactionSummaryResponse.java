package com.raissac.budget_management.transaction.dto;

import java.math.BigDecimal;
import java.time.Month;

public record MonthlyTransactionSummaryResponse(
        Month month,
        BigDecimal income,
        BigDecimal expenses
) {
}
