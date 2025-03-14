package com.raissac.budget_management.transaction.dto;

import com.raissac.budget_management.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String description,
        LocalDate date,
        TransactionType type,
        String categoryName
) {
}
