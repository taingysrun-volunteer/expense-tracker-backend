package com.taingy.expensetracker.repository;

import com.taingy.expensetracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, ExpenseRepositoryCustom {

    List<Expense> findAllByUserId(UUID userId);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Expense> searchUserExpenses(@Param("userId") UUID userId,
                                      @Param("searchTerm") String searchTerm,
                                      Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Expense> searchExpenses(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND " +
           "e.expenseDate BETWEEN :startDate AND :endDate")
    Page<Expense> findUserExpensesByDateRange(@Param("userId") UUID userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND " +
           "e.amount BETWEEN :minAmount AND :maxAmount")
    Page<Expense> findUserExpensesByAmountRange(@Param("userId") UUID userId,
                                                 @Param("minAmount") BigDecimal minAmount,
                                                 @Param("maxAmount") BigDecimal maxAmount,
                                                 Pageable pageable);

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

}
