package com.raissac.budget_management.transaction.dto;

import java.math.BigDecimal;

public record TopSpendingCategoryResponse(
        String categoryName,
        BigDecimal totalSpent
) {
}
