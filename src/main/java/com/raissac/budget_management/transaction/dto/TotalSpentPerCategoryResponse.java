package com.raissac.budget_management.transaction.dto;

import java.math.BigDecimal;

public record TotalSpentPerCategoryResponse(
        String categoryName,
        BigDecimal totalSpent
) {
}
