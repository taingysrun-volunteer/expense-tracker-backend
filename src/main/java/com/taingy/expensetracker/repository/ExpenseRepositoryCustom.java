package com.taingy.expensetracker.repository;

import com.taingy.expensetracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ExpenseRepositoryCustom {

    Page<Expense> findExpensesWithFilters(
            UUID userId,
            UUID categoryId,
            String searchTerm,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    );
}
