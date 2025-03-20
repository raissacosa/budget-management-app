package com.raissac.budget_management.category;

import com.raissac.budget_management.category.dto.ActiveCategoryResponse;
import com.raissac.budget_management.category.dto.CategoryRequest;
import com.raissac.budget_management.category.dto.CategoryResponse;
import com.raissac.budget_management.category.dto.CategoryUpdateRequest;
import com.raissac.budget_management.category.entity.Category;
import com.raissac.budget_management.category.repository.CategoryRepository;
import com.raissac.budget_management.category.service.CategoryService;
import com.raissac.budget_management.common.PageResponse;
import com.raissac.budget_management.exception.CategoryAlreadyExistsException;
import com.raissac.budget_management.exception.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryRequest categoryRequest;

    private CategoryUpdateRequest categoryUpdateRequest;

    @BeforeEach
    void setup() {
        categoryRequest = new CategoryRequest("Travel");
        categoryUpdateRequest = new CategoryUpdateRequest("Vacation", false);
    }

    @Test
    void shouldSaveCategory_whenCategoryRequestIsValid(){

        Category savedCategory = categoryService.createCategory(categoryRequest);

        assertNotNull(savedCategory.getId());
        assertEquals(categoryRequest.name(), savedCategory.getName());
        assertEquals(true, savedCategory.isActive());
    }

    @Test
    void shouldThrowException_whenCategoryAlreadyExists(){

        Category newCategory = Category.builder()
                .name("Travel")
                .active(true)
                .build();

        categoryRepository.save(newCategory);

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.createCategory(categoryRequest));

    }

    @Test
    void shouldUpdateCategory_whenCategoryUpdateRequestIsValid(){

        Category savedCategory = categoryService.createCategory(categoryRequest);

        Category updatedCategory = categoryService.updateCategory(savedCategory.getId(), categoryUpdateRequest);

        assertNotNull(updatedCategory.getId());
        assertEquals(categoryUpdateRequest.name(), updatedCategory.getName());
        assertEquals(categoryUpdateRequest.active(), updatedCategory.isActive());

    }

    @Test
    void shouldThrowException_whenCategoryIsNotFound(){

        assertThrows(CategoryNotFoundException.class, () -> categoryService.updateCategory(1L, categoryUpdateRequest));

    }

    @Test
    void shouldThrowException_whenCategoryAlreadyExistsOnUpdate(){

        Category travelCategory = Category.builder()
                .name("Travel")
                .active(true)
                .build();

        categoryRepository.save(travelCategory);

        Category vacationCategory = Category.builder()
                .name("Vacation")
                .active(true)
                .build();

        categoryRepository.save(vacationCategory);

        CategoryUpdateRequest request = new CategoryUpdateRequest("Travel", false);

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.updateCategory(vacationCategory.getId(), request));

    }

    @Test
    void shouldReturnPagedCategories_whenFindAllIsCalled() {

        categoryRepository.deleteAll();

        categoryRepository.save(Category.builder().name("Food").active(true).build());
        categoryRepository.save(Category.builder().name("Utilities").active(true).build());
        categoryRepository.save(Category.builder().name("Books").active(true).build());

        PageResponse<CategoryResponse> response = categoryService.findAllCategories(0, 2);

        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(2, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());

    }

    @Test
    void shouldReturnPagedCategories_whenFindAllActiveIsCalled() {

        categoryRepository.deleteAll();

        categoryRepository.save(Category.builder().name("Food").active(true).build());
        categoryRepository.save(Category.builder().name("Utilities").active(false).build());
        categoryRepository.save(Category.builder().name("Books").active(false).build());

        PageResponse<ActiveCategoryResponse> response = categoryService.findAllActiveCategories(0, 2);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());

    }


}
