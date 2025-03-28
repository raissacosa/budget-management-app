package com.raissac.budget_management.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raissac.budget_management.category.controller.CategoryController;
import com.raissac.budget_management.category.dto.CategoryRequest;
import com.raissac.budget_management.category.dto.CategoryResponse;
import com.raissac.budget_management.category.dto.CategoryUpdateRequest;
import com.raissac.budget_management.category.service.CategoryService;
import com.raissac.budget_management.common.PageResponse;
import com.raissac.budget_management.exception.CategoryNotFoundException;
import com.raissac.budget_management.security.config.CustomUserDetailsService;
import com.raissac.budget_management.security.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;


    @WithMockUser(roles = "ADMIN")
    @Test
    void createCategory_shouldReturn200_whenCategoryRequestIsValid() throws Exception {

        CategoryRequest request = new CategoryRequest("Food");

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Category added successfully"));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void createCategory_shouldReturn400_whenCategoryRequestIsInvalid() throws Exception {

        CategoryRequest request = new CategoryRequest("");

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void updateCategory_shouldReturn200_whenCategoryUpdateRequestIsValid() throws Exception {

        CategoryUpdateRequest request = new CategoryUpdateRequest("Food", false);

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Category updated successfully"));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void updateCategory_shouldReturn400_whenCategoryUpdateRequestIsInvalid() throws Exception {

        CategoryUpdateRequest request = new CategoryUpdateRequest("", null);

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void updateCategory_shouldReturn404_whenCategoryNotFound() throws Exception {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Test", true);

        doThrow(new CategoryNotFoundException("Category not found"))
                .when(categoryService).updateCategory(eq(99L), any());

        mockMvc.perform(put("/api/v1/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void findAllCategories_shouldReturn200AndListOfCategories_whenRequestIsValid() throws Exception {

        List<CategoryResponse> categories = List.of(
                new CategoryResponse(1L, "Food", true),
                new CategoryResponse(2L, "Books", false)
        );

        PageResponse<CategoryResponse> pageResponse = new PageResponse<>(
                categories,
                0,
                2,
                1,
                true,
                true
        );

        when(categoryService.findAllCategories(0, 10)).thenReturn(pageResponse);


        mockMvc.perform(get("/api/v1/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Food"))
                .andExpect(jsonPath("$.content[1].name").value("Books"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void findAllActiveCategories_shouldReturn200AndListOfActiveCategories_whenRequestIsValid() throws Exception {
        // Arrange
        List<CategoryResponse> categories = List.of(
                new CategoryResponse(1L, "Food", true),
                new CategoryResponse(2L, "Books", true)
        );

        PageResponse<CategoryResponse> pageResponse = new PageResponse<>(
                categories,
                0,
                2,
                1,
                true,
                true
        );

        when(categoryService.findAllCategories(0, 10)).thenReturn(pageResponse);

        // Act + Assert
        mockMvc.perform(get("/api/v1/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Food"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}
