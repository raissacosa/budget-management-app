package com.raissac.budget_management.category.dto;

import jakarta.validation.constraints.*;

public record CategoryUpdateRequest(
        @NotBlank(message = "Category name is required")
        String name,
        @NotNull(message = "Status must be provided")
        Boolean active
) {
}
