package com.raissac.budget_management.category.mapper;

import com.raissac.budget_management.category.dto.CategoryResponse;
import com.raissac.budget_management.category.entity.Category;
import org.springframework.stereotype.Service;

@Service
public class CategoryMapper {

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.isActive());
    }

}
