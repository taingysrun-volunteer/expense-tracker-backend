package com.taingy.expensetracker.repository;

import com.taingy.expensetracker.model.Category;
import com.taingy.expensetracker.model.Expense;
import com.taingy.expensetracker.model.Role;
import com.taingy.expensetracker.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseRepository expenseRepository;

    private User testUser;
    private Category testCategory;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        // Create and persist role
        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName("USER");
        userRole.setDescription("Regular user");
        entityManager.persist(userRole);

        // Create and persist user
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password");
        testUser.setRole(userRole);
        testUser.setIsActive(true);
        testUser.setIsVerified(true);
        entityManager.persist(testUser);

        // Create and persist category
        testCategory = new Category();
        testCategory.setName("Food");
        testCategory.setDescription("Food expenses");
        entityManager.persist(testCategory);

        // Create and persist expense
        testExpense = new Expense();
        testExpense.setTitle("Lunch");
        testExpense.setDescription("Team lunch at restaurant");
        testExpense.setAmount(new BigDecimal("50.00"));
        testExpense.setExpenseDate(LocalDate.now());
        testExpense.setUser(testUser);
        testExpense.setCategory(testCategory);
        entityManager.persist(testExpense);
        entityManager.flush();
    }

    @Test
    void findExpensesWithFilters_WithSearchTerm_ShouldReturnMatchingExpenses() {
        // Act
        Page<Expense> result = expenseRepository.findExpensesWithFilters(
                null, null, "lunch", null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Lunch");
    }

    @Test
    void findExpensesWithFilters_WithUserId_ShouldReturnUserExpenses() {
        // Act
        Page<Expense> result = expenseRepository.findExpensesWithFilters(
                testUser.getId(), null, null, null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findExpensesWithFilters_WithCategoryId_ShouldReturnCategoryExpenses() {
        // Act
        Page<Expense> result = expenseRepository.findExpensesWithFilters(
                null, testCategory.getId(), null, null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory().getId()).isEqualTo(testCategory.getId());
    }

    @Test
    void findExpensesWithFilters_WithDateRange_ShouldReturnFilteredExpenses() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        // Act
        Page<Expense> result = expenseRepository.findExpensesWithFilters(
                null, null, null, startDate, endDate, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getExpenseDate()).isBetween(startDate, endDate);
    }

    @Test
    void findExpensesWithFilters_WithAmountRange_ShouldReturnFilteredExpenses() {
        // Arrange
        BigDecimal minAmount = new BigDecimal("10.00");
        BigDecimal maxAmount = new BigDecimal("100.00");

        // Act
        Page<Expense> result = expenseRepository.findExpensesWithFilters(
                null, null, null, null, null, minAmount, maxAmount, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAmount()).isBetween(minAmount, maxAmount);
    }

    @Test
    void findExpensesWithFilters_WithMultipleFilters_ShouldReturnFilteredExpenses() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        BigDecimal minAmount = new BigDecimal("10.00");
        BigDecimal maxAmount = new BigDecimal("100.00");

        // Act
        Page<Expense> result = expenseRepository.findExpensesWithFilters(
                testUser.getId(), testCategory.getId(), "lunch",
                startDate, endDate, minAmount, maxAmount, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Lunch");
    }

    @Test
    void findExpensesWithFilters_WithNoFilters_ShouldReturnAllExpenses() {
        // Act
        Page<Expense> result = expenseRepository.findExpensesWithFilters(
                null, null, null, null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findExpensesWithFilters_WithNonMatchingSearch_ShouldReturnEmpty() {
        // Act
        Page<Expense> result = expenseRepository.findExpensesWithFilters(
                null, null, "nonexistent", null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findExpensesWithFilters_WithOutOfRangeDates_ShouldReturnEmpty() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now().minusDays(5);

        // Act
        Page<Expense> result = expenseRepository.findExpensesWithFilters(
                null, null, null, startDate, endDate, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findExpensesWithFilters_WithOutOfRangeAmounts_ShouldReturnEmpty() {
        // Arrange
        BigDecimal minAmount = new BigDecimal("100.00");
        BigDecimal maxAmount = new BigDecimal("200.00");

        // Act
        Page<Expense> result = expenseRepository.findExpensesWithFilters(
                null, null, null, null, null, minAmount, maxAmount, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).isEmpty();
    }
}
