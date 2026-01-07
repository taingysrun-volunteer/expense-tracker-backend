package com.taingy.expensetracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ExpenseResponse {

    private UUID id;
    private LocalDate expenseDate;
    private String title;
    private String description;
    private BigDecimal amount;
    private String categoryName;
    private UUID userId;
    private String userEmail;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
