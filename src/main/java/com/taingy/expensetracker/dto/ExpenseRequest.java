package com.taingy.expensetracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ExpenseRequest {

    private UUID userId;
    private String title;
    private BigDecimal amount;
    private UUID categoryId;
    private String description;
    private LocalDate expenseDate;

}
