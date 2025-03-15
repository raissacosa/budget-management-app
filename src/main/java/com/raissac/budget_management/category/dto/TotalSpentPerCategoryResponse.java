package com.raissac.budget_management.category.dto;

import java.math.BigDecimal;

public record TotalSpentPerCategoryResponse(
        String categoryName,
        BigDecimal totalSpent
) {
}
