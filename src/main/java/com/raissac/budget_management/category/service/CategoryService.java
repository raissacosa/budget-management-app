package com.raissac.budget_management.category.service;

import com.raissac.budget_management.category.dto.ActiveCategoryResponse;
import com.raissac.budget_management.category.dto.CategoryRequest;
import com.raissac.budget_management.category.dto.CategoryResponse;
import com.raissac.budget_management.category.dto.CategoryUpdateRequest;
import com.raissac.budget_management.category.entity.Category;
import com.raissac.budget_management.category.mapper.CategoryMapper;
import com.raissac.budget_management.category.repository.CategoryRepository;
import com.raissac.budget_management.common.PageResponse;
import com.raissac.budget_management.exception.CategoryAlreadyExistsException;
import com.raissac.budget_management.exception.CategoryNotFoundException;
import com.raissac.budget_management.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

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
        Category savedCategory = categoryRepository.save(newCategory);

        logger.info("Category with name {} created successfully", savedCategory.getName());

        return savedCategory;
    }

    public Category updateCategory(Long id, CategoryUpdateRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("The category does not exist!"));

        categoryRepository.findByName(request.name())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(e -> {
                    throw new CategoryAlreadyExistsException("Category with name " + request.name() + " already exists");
                });

        category.setName(request.name());
        category.setActive(request.active());

        Category savedCategory = categoryRepository.save(category);

        logger.info("Category updated: id={}, name={}, active={}", savedCategory.getId(), savedCategory.getName(), savedCategory.isActive());

        return savedCategory;
    }

    public PageResponse<CategoryResponse> findAllCategories(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Category> categories = categoryRepository.findAll(pageable);

        List<CategoryResponse> categoryResponseList = categories
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();

        logger.info("Fetched {} categories", categoryResponseList.size());

        return new PageResponse<>(categoryResponseList,
                categories.getNumber(),
                categories.getNumberOfElements(),
                categories.getTotalPages(),
                categories.isFirst(),
                categories.isLast());
    }

    public PageResponse<ActiveCategoryResponse> findAllActiveCategories(int page, int size){

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Category> categories = categoryRepository.findAllByActiveTrue(pageable);

        List<ActiveCategoryResponse> categoryResponseList = categories
                .stream()
                .map(categoryMapper::toCategoryActiveResponse)
                .toList();

        logger.info("Fetched {} active categories", categoryResponseList.size());

        return new PageResponse<>(categoryResponseList,
                categories.getNumber(),
                categories.getNumberOfElements(),
                categories.getTotalPages(),
                categories.isFirst(),
                categories.isLast());

    }
}
