package com.raissac.budget_management.category.dto;

public record CategoryResponse (
    Long id,
    String name,
    Boolean active
) {
}
