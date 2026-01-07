package com.taingy.expensetracker.mapper;


import com.taingy.expensetracker.dto.ExpenseRequest;
import com.taingy.expensetracker.dto.ExpenseResponse;
import com.taingy.expensetracker.model.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseRequest dto) {
        if (dto == null) return null;

        Expense expense = new Expense();
        expense.setTitle(dto.getTitle());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());

        return expense;
    }

    public ExpenseResponse toDto(Expense expense) {
        if (expense == null) return null;

        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .expenseDate(expense.getExpenseDate())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .categoryName(expense.getCategory().getName())
                .userId(expense.getUser().getId())
                .userEmail(expense.getUser().getEmail())
                .userName(expense.getUser().getFirstName() + " " + expense.getUser().getLastName())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
