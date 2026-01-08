package com.taingy.expensetracker.service.impl;

import com.taingy.expensetracker.dto.ExpenseRequest;
import com.taingy.expensetracker.dto.ExpenseResponse;
import com.taingy.expensetracker.dto.ExpenseSummary;
import com.taingy.expensetracker.mapper.ExpenseMapper;
import com.taingy.expensetracker.model.Category;
import com.taingy.expensetracker.model.Expense;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.repository.CategoryRepository;
import com.taingy.expensetracker.repository.ExpenseRepository;
import com.taingy.expensetracker.repository.UserRepository;
import com.taingy.expensetracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final UserRepository userRepository;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CategoryRepository categoryRepository, ExpenseMapper expenseMapper, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.expenseMapper = expenseMapper;
        this.userRepository = userRepository;
    }


    @Override
    public ExpenseResponse findById(UUID id) {
        Expense expense = expenseRepository.findById(id).orElse(null);
        return expenseMapper.toDto(expense);
    }

    @Override
    public Page<ExpenseResponse> findExpensesWithFilters(UUID userId, UUID categoryId, String searchTerm,
                                                          LocalDate startDate, LocalDate endDate,
                                                          BigDecimal minAmount, BigDecimal maxAmount,
                                                          Pageable pageable) {
        return expenseRepository.findExpensesWithFilters(userId, categoryId, searchTerm,
                        startDate, endDate, minAmount, maxAmount, pageable)
                .map(expenseMapper::toDto);
    }

    @Override
    @Transactional
    public ExpenseResponse create(ExpenseRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);

        Expense expense = expenseMapper.toEntity(request);
        expense.setUser(user);
        expense.setCategory(category);
        expense = expenseRepository.save(expense);
        return expenseMapper.toDto(expense);
    }

    @Override
    @Transactional
    public ExpenseResponse update(UUID id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Expense not found with id: " + id)
        );
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getTitle() != null) {
            expense.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }
        Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        expense.setCategory(category);
        expense = expenseRepository.save(expense);

        return expenseMapper.toDto(expense);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        expenseRepository.delete(expense);
    }

    @Override
    public ExpenseSummary getSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        UUID userId = user.getId();
        String roleName = user.getRole().getName();

        // Get basic statistics based on role
        BigDecimal totalAmount;
        Long totalCount;
        BigDecimal averageAmount;
        BigDecimal maxAmount;
        BigDecimal minAmount;
        List<Expense> allExpenses;

        if ("ADMIN".equals(roleName)) {
            // Admin gets all expenses across all users
            totalAmount = expenseRepository.getTotalAmount();
            totalCount = expenseRepository.count();
            averageAmount = expenseRepository.getAverageAmount();
            maxAmount = expenseRepository.getMaxAmount();
            minAmount = expenseRepository.getMinAmount();
            allExpenses = expenseRepository.findAll();
        } else {
            // Regular users get only their expenses
            totalAmount = expenseRepository.getTotalAmountByUserId(userId);
            totalCount = expenseRepository.getCountByUserId(userId);
            averageAmount = expenseRepository.getAverageAmountByUserId(userId);
            maxAmount = expenseRepository.getMaxAmountByUserId(userId);
            minAmount = expenseRepository.getMinAmountByUserId(userId);
            allExpenses = expenseRepository.findAllByUserId(userId);
        }

        // Handle null values (when no expenses exist)
        totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        totalCount = totalCount != null ? totalCount : 0L;
        averageAmount = averageAmount != null ? averageAmount : BigDecimal.ZERO;
        maxAmount = maxAmount != null ? maxAmount : BigDecimal.ZERO;
        minAmount = minAmount != null ? minAmount : BigDecimal.ZERO;

        // Calculate category breakdown
        List<ExpenseSummary.CategorySummary> categoryBreakdown = calculateCategoryBreakdown(allExpenses, totalAmount);

        // Calculate monthly breakdown
        List<ExpenseSummary.MonthlySummary> monthlyBreakdown = calculateMonthlyBreakdown(allExpenses);

        // Calculate user breakdown (only for admins)
        List<ExpenseSummary.UserSummary> userBreakdown = null;
        if ("ADMIN".equals(roleName)) {
            userBreakdown = calculateUserBreakdown(allExpenses, totalAmount);
        }

        return ExpenseSummary.builder()
                .totalAmount(totalAmount)
                .totalCount(totalCount)
                .averageAmount(averageAmount)
                .maxAmount(maxAmount)
                .minAmount(minAmount)
                .categoryBreakdown(categoryBreakdown)
                .monthlyBreakdown(monthlyBreakdown)
                .userBreakdown(userBreakdown)
                .build();
    }

    private List<ExpenseSummary.CategorySummary> calculateCategoryBreakdown(List<Expense> expenses, BigDecimal totalAmount) {
        Map<String, List<Expense>> groupedByCategory = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getCategory() != null ? e.getCategory().getName() : "Uncategorized"));

        return groupedByCategory.entrySet().stream()
                .map(entry -> {
                    String categoryName = entry.getKey();
                    List<Expense> categoryExpenses = entry.getValue();

                    BigDecimal categoryTotal = categoryExpenses.stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long count = categoryExpenses.size();

                    double percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? categoryTotal.divide(totalAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;

                    return ExpenseSummary.CategorySummary.builder()
                            .categoryName(categoryName)
                            .totalAmount(categoryTotal)
                            .count(count)
                            .percentage(percentage)
                            .build();
                })
                .sorted(Comparator.comparing(ExpenseSummary.CategorySummary::getTotalAmount).reversed())
                .collect(Collectors.toList());
    }

    private List<ExpenseSummary.MonthlySummary> calculateMonthlyBreakdown(List<Expense> expenses) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, List<Expense>> groupedByMonth = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getExpenseDate().format(formatter)));

        return groupedByMonth.entrySet().stream()
                .map(entry -> {
                    String month = entry.getKey();
                    List<Expense> monthExpenses = entry.getValue();

                    BigDecimal monthTotal = monthExpenses.stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long count = monthExpenses.size();

                    return ExpenseSummary.MonthlySummary.builder()
                            .month(month)
                            .totalAmount(monthTotal)
                            .count(count)
                            .build();
                })
                .sorted(Comparator.comparing(ExpenseSummary.MonthlySummary::getMonth).reversed())
                .collect(Collectors.toList());
    }

    private List<ExpenseSummary.UserSummary> calculateUserBreakdown(List<Expense> expenses, BigDecimal totalAmount) {
        Map<User, List<Expense>> groupedByUser = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getUser));

        return groupedByUser.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<Expense> userExpenses = entry.getValue();

                    BigDecimal userTotal = userExpenses.stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long count = userExpenses.size();

                    double percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? userTotal.divide(totalAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;

                    return ExpenseSummary.UserSummary.builder()
                            .userName(user.getFirstName() + " " + user.getLastName())
                            .userEmail(user.getEmail())
                            .totalAmount(userTotal)
                            .count(count)
                            .percentage(percentage)
                            .build();
                })
                .sorted(Comparator.comparing(ExpenseSummary.UserSummary::getTotalAmount).reversed())
                .collect(Collectors.toList());
    }
}
