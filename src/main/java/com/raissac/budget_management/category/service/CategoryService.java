package com.raissac.budget_management.category.service;

import com.raissac.budget_management.category.dto.CategoryRequest;
import com.raissac.budget_management.category.entity.Category;
import com.raissac.budget_management.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category createCategory(CategoryRequest request) {
        Optional<Category> existingCategory = categoryRepository.findByName(request.name());
        existingCategory.ifPresent(category -> {
            String status = category.isActive() ? "active" : "inactive";
            throw new RuntimeException("Category with name " + request.name() + " already exists and is " + status);
        });

        Category newCategory = Category.builder()
                .name(request.name())
                .active(true)
                .build();
        return categoryRepository.save(newCategory);
    }
}
