package com.raissac.budget_management.transaction.dto;

import com.raissac.budget_management.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater then 0")
        BigDecimal amount,
        @NotNull(message = "Date is required")
        LocalDate date,
        String description,
        @NotNull(message = "Type is required")
        TransactionType type,
        @NotNull(message = "Category is required")
        Long categoryId
) {
}
