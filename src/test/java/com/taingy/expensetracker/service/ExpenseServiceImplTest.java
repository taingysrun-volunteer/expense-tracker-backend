package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.ExpenseRequest;
import com.taingy.expensetracker.dto.ExpenseResponse;
import com.taingy.expensetracker.mapper.ExpenseMapper;
import com.taingy.expensetracker.model.Category;
import com.taingy.expensetracker.model.Expense;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.repository.ExpenseRepository;
import com.taingy.expensetracker.repository.UserRepository;
import com.taingy.expensetracker.service.impl.ExpenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Expense testExpense;
    private ExpenseRequest expenseRequest;
    private ExpenseResponse expenseResponse;
    private UUID expenseId;
    private UUID userId;
    private UUID categoryId;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testCategory = new Category();
        testCategory.setId(categoryId);
        testCategory.setName("Food");

        testExpense = new Expense();
        testExpense.setId(expenseId);
        testExpense.setTitle("Lunch");
        testExpense.setDescription("Team lunch");
        testExpense.setAmount(new BigDecimal("50.00"));
        testExpense.setExpenseDate(LocalDate.now());
        testExpense.setUser(testUser);
        testExpense.setCategory(testCategory);
        testExpense.setCreatedAt(LocalDateTime.now());
        testExpense.setUpdatedAt(LocalDateTime.now());

        expenseRequest = new ExpenseRequest();
        expenseRequest.setAmount(new BigDecimal("50.00"));
        expenseRequest.setCategoryId(categoryId);
        expenseRequest.setDescription("Team lunch");
        expenseRequest.setExpenseDate(LocalDate.now());

        expenseResponse = ExpenseResponse.builder()
                .id(expenseId)
                .expenseDate(LocalDate.now())
                .description("Team lunch")
                .amount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findById_ShouldReturnExpenseResponse_WhenExpenseExists() {
        // Arrange
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(expenseResponse);

        // Act
        ExpenseResponse result = expenseService.findById(expenseId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expenseId);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        verify(expenseRepository).findById(expenseId);
    }

    @Test
    void findById_ShouldReturnNull_WhenExpenseDoesNotExist() {
        // Arrange
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());
        when(expenseMapper.toDto(null)).thenReturn(null);

        // Act
        ExpenseResponse result = expenseService.findById(expenseId);

        // Assert
        assertThat(result).isNull();
        verify(expenseRepository).findById(expenseId);
    }

    @Test
    void findExpensesWithFilters_ShouldReturnPagedExpenses() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> expensePage = new PageImpl<>(Arrays.asList(testExpense));

        when(expenseRepository.findExpensesWithFilters(
                any(), any(), anyString(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(expensePage);
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(expenseResponse);

        // Act
        Page<ExpenseResponse> result = expenseService.findExpensesWithFilters(
                userId, categoryId, "lunch", null, null, null, null, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(expenseId);
        verify(expenseRepository).findExpensesWithFilters(
                userId, categoryId, "lunch", null, null, null, null, pageable);
    }

    @Test
    void findExpensesWithFilters_WithDateRange_ShouldReturnFilteredExpenses() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        Page<Expense> expensePage = new PageImpl<>(Arrays.asList(testExpense));

        when(expenseRepository.findExpensesWithFilters(
                any(), any(), any(), eq(startDate), eq(endDate), any(), any(), any(Pageable.class)))
                .thenReturn(expensePage);
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(expenseResponse);

        // Act
        Page<ExpenseResponse> result = expenseService.findExpensesWithFilters(
                userId, null, null, startDate, endDate, null, null, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(expenseRepository).findExpensesWithFilters(
                userId, null, null, startDate, endDate, null, null, pageable);
    }

    @Test
    void findExpensesWithFilters_WithAmountRange_ShouldReturnFilteredExpenses() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        BigDecimal minAmount = new BigDecimal("10.00");
        BigDecimal maxAmount = new BigDecimal("100.00");
        Page<Expense> expensePage = new PageImpl<>(Arrays.asList(testExpense));

        when(expenseRepository.findExpensesWithFilters(
                any(), any(), any(), any(), any(), eq(minAmount), eq(maxAmount), any(Pageable.class)))
                .thenReturn(expensePage);
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(expenseResponse);

        // Act
        Page<ExpenseResponse> result = expenseService.findExpensesWithFilters(
                userId, null, null, null, null, minAmount, maxAmount, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(expenseRepository).findExpensesWithFilters(
                userId, null, null, null, null, minAmount, maxAmount, pageable);
    }

    @Test
    void create_ShouldCreateExpenseSuccessfully() {
        // Arrange
        String userEmail = "test@example.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(expenseMapper.toEntity(any(ExpenseRequest.class))).thenReturn(testExpense);
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(expenseResponse);

        // Act
        ExpenseResponse result = expenseService.create(expenseRequest, userEmail);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expenseId);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        verify(userRepository).findByEmail(userEmail);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String userEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.create(expenseRequest, userEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found with email: " + userEmail);

        verify(userRepository).findByEmail(userEmail);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void update_ShouldUpdateExpenseSuccessfully() {
        // Arrange
        when(expenseMapper.toEntity(any(ExpenseRequest.class))).thenReturn(testExpense);
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseMapper.toDto(any(Expense.class))).thenReturn(expenseResponse);

        // Act
        ExpenseResponse result = expenseService.update(expenseId, expenseRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(expenseId);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void delete_ShouldDeleteExpense_WhenExpenseExists() {
        // Arrange
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));

        // Act
        expenseService.delete(expenseId);

        // Assert
        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository).delete(testExpense);
    }

    @Test
    void delete_ShouldThrowException_WhenExpenseDoesNotExist() {
        // Arrange
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.delete(expenseId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expense not found");

        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository, never()).delete(any(Expense.class));
    }
}
