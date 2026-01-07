package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.CategoryRequest;
import com.taingy.expensetracker.dto.CategoryResponse;
import com.taingy.expensetracker.mapper.CategoryMapper;
import com.taingy.expensetracker.model.Category;
import com.taingy.expensetracker.repository.CategoryRepository;
import com.taingy.expensetracker.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        testCategory = new Category();
        testCategory.setId(categoryId);
        testCategory.setName("Food");
        testCategory.setDescription("Food and dining expenses");
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Food");
        categoryRequest.setDescription("Food and dining expenses");

        categoryResponse = CategoryResponse.builder()
                .id(categoryId)
                .name("Food")
                .description("Food and dining expenses")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        // Arrange
        Category category2 = new Category();
        category2.setId(UUID.randomUUID());
        category2.setName("Transport");

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory, category2));
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryResponse);

        // Act
        List<CategoryResponse> result = categoryService.findAll();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(categoryRepository).findAll();
        verify(categoryMapper, times(2)).toDto(any(Category.class));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoCategoriesExist() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<CategoryResponse> result = categoryService.findAll();

        // Assert
        assertThat(result).isEmpty();
        verify(categoryRepository).findAll();
    }

    @Test
    void findById_ShouldReturnCategory_WhenCategoryExists() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryService.findById(categoryId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Food");
        assertThat(result.getId()).isEqualTo(categoryId);
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void findById_ShouldReturnNull_WhenCategoryDoesNotExist() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        when(categoryMapper.toDto(null)).thenReturn(null);

        // Act
        CategoryResponse result = categoryService.findById(categoryId);

        // Assert
        assertThat(result).isNull();
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void create_ShouldCreateCategorySuccessfully() {
        // Arrange
        when(categoryMapper.toEntity(any(CategoryRequest.class))).thenReturn(testCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryService.create(categoryRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Food");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void update_ShouldUpdateCategorySuccessfully() {
        // Arrange
        when(categoryMapper.toEntity(any(CategoryRequest.class))).thenReturn(testCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryService.update(categoryId, categoryRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void delete_ShouldDeleteCategory_WhenCategoryExists() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // Act
        categoryService.delete(categoryId);

        // Assert
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void delete_ShouldThrowException_WhenCategoryDoesNotExist() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.delete(categoryId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category with id: " + categoryId + " does not exist");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
