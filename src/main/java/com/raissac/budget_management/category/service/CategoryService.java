package com.raissac.budget_management.category.service;

import com.raissac.budget_management.category.dto.CategoryRequest;
import com.raissac.budget_management.category.dto.CategoryUpdateRequest;
import com.raissac.budget_management.category.entity.Category;
import com.raissac.budget_management.category.repository.CategoryRepository;
import com.raissac.budget_management.exception.CategoryAlreadyExistsException;
import com.raissac.budget_management.exception.CategoryNotFoundException;
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
            throw new CategoryAlreadyExistsException("Category with name " + request.name() + " already exists and is " + status);
        });

        Category newCategory = Category.builder()
                .name(request.name())
                .active(true)
                .build();
        return categoryRepository.save(newCategory);
    }

    public Category updateCategory(Long id, CategoryUpdateRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("The category does not exists!"));

        categoryRepository.findByName(request.name())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(e -> {
                    throw new CategoryAlreadyExistsException("Category with name " + request.name() + " already exists");
                });

        category.setName(request.name());
        category.setActive(request.active());

        return categoryRepository.save(category);
    }
}
