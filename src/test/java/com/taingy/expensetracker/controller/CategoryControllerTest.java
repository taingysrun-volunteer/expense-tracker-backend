package com.taingy.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taingy.expensetracker.dto.CategoryRequest;
import com.taingy.expensetracker.dto.CategoryResponse;
import com.taingy.expensetracker.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private CategoryResponse categoryResponse;
    private CategoryRequest categoryRequest;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        categoryResponse = CategoryResponse.builder()
                .id(categoryId)
                .name("Food")
                .description("Food and dining expenses")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Food");
        categoryRequest.setDescription("Food and dining expenses");
    }

    @Test
    @WithMockUser
    void getCategories_ShouldReturnAllCategories() throws Exception {
        // Arrange
        when(categoryService.findAll()).thenReturn(Arrays.asList(categoryResponse));

        // Act & Assert
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[0].description").value("Food and dining expenses"));

        verify(categoryService).findAll();
    }

    @Test
    @WithMockUser
    void getCategories_ShouldReturnEmptyList_WhenNoCategoriesExist() throws Exception {
        // Arrange
        when(categoryService.findAll()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(categoryService).findAll();
    }

    @Test
    @WithMockUser
    void getCategory_ShouldReturnCategory_WhenCategoryExists() throws Exception {
        // Arrange
        when(categoryService.findById(categoryId)).thenReturn(categoryResponse);

        // Act & Assert
        mockMvc.perform(get("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Food"));

        verify(categoryService).findById(categoryId);
    }

    @Test
    @WithMockUser
    void createCategory_ShouldCreateCategorySuccessfully() throws Exception {
        // Arrange
        when(categoryService.create(any(CategoryRequest.class))).thenReturn(categoryResponse);

        // Act & Assert
        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Food"));

        verify(categoryService).create(any(CategoryRequest.class));
    }

    @Test
    @WithMockUser
    void updateCategory_ShouldUpdateCategorySuccessfully() throws Exception {
        // Arrange
        when(categoryService.update(eq(categoryId), any(CategoryRequest.class))).thenReturn(categoryResponse);

        // Act & Assert
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Food"));

        verify(categoryService).update(eq(categoryId), any(CategoryRequest.class));
    }

    @Test
    @WithMockUser
    void deleteCategory_ShouldDeleteCategorySuccessfully() throws Exception {
        // Arrange
        doNothing().when(categoryService).delete(categoryId);

        // Act & Assert
        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully deleted the category with id: " + categoryId));

        verify(categoryService).delete(categoryId);
    }
}
