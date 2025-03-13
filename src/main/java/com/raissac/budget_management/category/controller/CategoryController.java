package com.raissac.budget_management.category.controller;

import com.raissac.budget_management.category.dto.CategoryRequest;
import com.raissac.budget_management.category.dto.CategoryUpdateRequest;
import com.raissac.budget_management.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<String> createCategory(@Valid @RequestBody CategoryRequest category) {
        categoryService.createCategory(category);
        return ResponseEntity.ok("Category added successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest category) {
        categoryService.updateCategory(id, category);
        return ResponseEntity.ok("Category updated successfully");
    }
}
