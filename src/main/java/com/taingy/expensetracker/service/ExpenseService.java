package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.ExpenseRequest;
import com.taingy.expensetracker.dto.ExpenseResponse;
import com.taingy.expensetracker.dto.ExpenseSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ExpenseService {

    ExpenseResponse findById(UUID id);
    Page<ExpenseResponse> findExpensesWithFilters(UUID userId, UUID categoryId, String searchTerm,
                                                   LocalDate startDate, LocalDate endDate,
                                                   BigDecimal minAmount, BigDecimal maxAmount,
                                                   Pageable pageable);
    ExpenseResponse create(ExpenseRequest request, String userEmail);
    ExpenseResponse update(UUID id, ExpenseRequest request);
    void delete(UUID id);
    ExpenseSummary getSummary(String userEmail);

}
