package com.raissac.budget_management.transaction.dto;

import com.raissac.budget_management.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionFilterRequest(
        BigDecimal minAmount,
        BigDecimal maxAmount,
        LocalDate startDate,
        LocalDate endDate,
        TransactionType type,
        Long categoryId
) {
}
