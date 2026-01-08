package com.taingy.expensetracker.repository;

import com.taingy.expensetracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, ExpenseRepositoryCustom {

    List<Expense> findAllByUserId(UUID userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getTotalAmountByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId")
    Long getCountByUserId(@Param("userId") UUID userId);

    @Query("SELECT AVG(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getAverageAmountByUserId(@Param("userId") UUID userId);

    @Query("SELECT MAX(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getMaxAmountByUserId(@Param("userId") UUID userId);

    @Query("SELECT MIN(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getMinAmountByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(e.amount) FROM Expense e")
    BigDecimal getTotalAmount();

    @Query("SELECT AVG(e.amount) FROM Expense e")
    BigDecimal getAverageAmount();

    @Query("SELECT MAX(e.amount) FROM Expense e")
    BigDecimal getMaxAmount();

    @Query("SELECT MIN(e.amount) FROM Expense e")
    BigDecimal getMinAmount();

}
