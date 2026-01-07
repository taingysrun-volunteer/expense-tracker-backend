package com.taingy.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taingy.expensetracker.dto.ExpenseRequest;
import com.taingy.expensetracker.dto.ExpenseResponse;
import com.taingy.expensetracker.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    private ExpenseResponse expenseResponse;
    private ExpenseRequest expenseRequest;
    private UUID expenseId;
    private UUID userId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        expenseResponse = ExpenseResponse.builder()
                .id(expenseId)
                .expenseDate(LocalDate.now())
                .description("Team lunch")
                .amount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        expenseRequest = new ExpenseRequest();
        expenseRequest.setAmount(new BigDecimal("50.00"));
        expenseRequest.setCategoryId(categoryId);
        expenseRequest.setDescription("Team lunch");
        expenseRequest.setExpenseDate(LocalDate.now());
    }

    @Test
    @WithMockUser
    void findAll_ShouldReturnPagedExpenses() throws Exception {
        // Arrange
        Page<ExpenseResponse> expensePage = new PageImpl<>(Arrays.asList(expenseResponse));
        when(expenseService.findExpensesWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expensePage);

        // Act & Assert
        mockMvc.perform(get("/api/expenses")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(expenseId.toString()))
                .andExpect(jsonPath("$.content[0].description").value("Team lunch"))
                .andExpect(jsonPath("$.content[0].amount").value(50.00));

        verify(expenseService).findExpensesWithFilters(
                null, null, null, null, null, null, null, PageRequest.of(0, 10));
    }

    @Test
    @WithMockUser
    void findAll_WithUserId_ShouldReturnFilteredExpenses() throws Exception {
        // Arrange
        Page<ExpenseResponse> expensePage = new PageImpl<>(Arrays.asList(expenseResponse));
        when(expenseService.findExpensesWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expensePage);

        // Act & Assert
        mockMvc.perform(get("/api/expenses")
                        .param("userId", userId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(expenseId.toString()));

        verify(expenseService).findExpensesWithFilters(
                eq(userId), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    void findAll_WithSearchTerm_ShouldReturnFilteredExpenses() throws Exception {
        // Arrange
        Page<ExpenseResponse> expensePage = new PageImpl<>(Arrays.asList(expenseResponse));
        when(expenseService.findExpensesWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expensePage);

        // Act & Assert
        mockMvc.perform(get("/api/expenses")
                        .param("searchTerm", "lunch")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Team lunch"));

        verify(expenseService).findExpensesWithFilters(
                any(), any(), eq("lunch"), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    void findAll_WithDateRange_ShouldReturnFilteredExpenses() throws Exception {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        Page<ExpenseResponse> expensePage = new PageImpl<>(Arrays.asList(expenseResponse));
        when(expenseService.findExpensesWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expensePage);

        // Act & Assert
        mockMvc.perform(get("/api/expenses")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(expenseService).findExpensesWithFilters(
                any(), any(), any(), eq(startDate), eq(endDate), any(), any(), any());
    }

    @Test
    @WithMockUser
    void findAll_WithAmountRange_ShouldReturnFilteredExpenses() throws Exception {
        // Arrange
        BigDecimal minAmount = new BigDecimal("10.00");
        BigDecimal maxAmount = new BigDecimal("100.00");
        Page<ExpenseResponse> expensePage = new PageImpl<>(Arrays.asList(expenseResponse));
        when(expenseService.findExpensesWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(expensePage);

        // Act & Assert
        mockMvc.perform(get("/api/expenses")
                        .param("minAmount", minAmount.toString())
                        .param("maxAmount", maxAmount.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(expenseService).findExpensesWithFilters(
                any(), any(), any(), any(), any(), eq(minAmount), eq(maxAmount), any());
    }

    @Test
    @WithMockUser
    void findById_ShouldReturnExpense_WhenExpenseExists() throws Exception {
        // Arrange
        when(expenseService.findById(expenseId)).thenReturn(expenseResponse);

        // Act & Assert
        mockMvc.perform(get("/api/expenses/{id}", expenseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()))
                .andExpect(jsonPath("$.description").value("Team lunch"));

        verify(expenseService).findById(expenseId);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void create_ShouldCreateExpenseSuccessfully() throws Exception {
        // Arrange
        when(expenseService.create(any(ExpenseRequest.class), any(String.class))).thenReturn(expenseResponse);

        // Act & Assert
        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()))
                .andExpect(jsonPath("$.amount").value(50.00));

        verify(expenseService).create(any(ExpenseRequest.class), eq("test@example.com"));
    }

    @Test
    @WithMockUser
    void update_ShouldUpdateExpenseSuccessfully() throws Exception {
        // Arrange
        when(expenseService.update(eq(expenseId), any(ExpenseRequest.class))).thenReturn(expenseResponse);

        // Act & Assert
        mockMvc.perform(put("/api/expenses/{id}", expenseId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()));

        verify(expenseService).update(eq(expenseId), any(ExpenseRequest.class));
    }

    @Test
    @WithMockUser
    void delete_ShouldDeleteExpenseSuccessfully() throws Exception {
        // Arrange
        doNothing().when(expenseService).delete(expenseId);

        // Act & Assert
        mockMvc.perform(delete("/api/expenses/{id}", expenseId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully deleted Expense"));

        verify(expenseService).delete(expenseId);
    }
}
