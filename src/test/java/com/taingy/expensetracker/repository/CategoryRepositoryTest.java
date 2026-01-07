package com.taingy.expensetracker.repository;

import com.taingy.expensetracker.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Food");
        testCategory.setDescription("Food and dining expenses");
        entityManager.persist(testCategory);
        entityManager.flush();
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        // Arrange
        Category category2 = new Category();
        category2.setName("Transport");
        category2.setDescription("Transportation expenses");
        entityManager.persist(category2);
        entityManager.flush();

        // Act
        List<Category> categories = categoryRepository.findAll();

        // Assert
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(Category::getName)
                .containsExactlyInAnyOrder("Food", "Transport");
    }

    @Test
    void findById_ShouldReturnCategory_WhenCategoryExists() {
        // Act
        Optional<Category> found = categoryRepository.findById(testCategory.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Food");
        assertThat(found.get().getDescription()).isEqualTo("Food and dining expenses");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenCategoryDoesNotExist() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act
        Optional<Category> found = categoryRepository.findById(nonExistentId);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void save_ShouldPersistNewCategory() {
        // Arrange
        Category newCategory = new Category();
        newCategory.setName("Entertainment");
        newCategory.setDescription("Entertainment and leisure");

        // Act
        Category saved = categoryRepository.save(newCategory);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Entertainment");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_ShouldUpdateExistingCategory() {
        // Arrange
        testCategory.setName("Food & Beverages");
        testCategory.setDescription("Food, drinks and dining");

        // Act
        Category updated = categoryRepository.save(testCategory);

        // Assert
        assertThat(updated.getId()).isEqualTo(testCategory.getId());
        assertThat(updated.getName()).isEqualTo("Food & Beverages");
        assertThat(updated.getDescription()).isEqualTo("Food, drinks and dining");
    }

    @Test
    void delete_ShouldRemoveCategory() {
        // Arrange
        UUID categoryId = testCategory.getId();

        // Act
        categoryRepository.delete(testCategory);
        entityManager.flush();

        // Assert
        Optional<Category> found = categoryRepository.findById(categoryId);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoCategoriesExist() {
        // Arrange
        categoryRepository.deleteAll();
        entityManager.flush();

        // Act
        List<Category> categories = categoryRepository.findAll();

        // Assert
        assertThat(categories).isEmpty();
    }

    @Test
    void existsById_ShouldReturnTrue_WhenCategoryExists() {
        // Act
        boolean exists = categoryRepository.existsById(testCategory.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalse_WhenCategoryDoesNotExist() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act
        boolean exists = categoryRepository.existsById(nonExistentId);

        // Assert
        assertThat(exists).isFalse();
    }
}
