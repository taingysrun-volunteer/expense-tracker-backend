package com.taingy.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummary {

    private BigDecimal totalAmount;
    private Long totalCount;
    private BigDecimal averageAmount;
    private BigDecimal maxAmount;
    private BigDecimal minAmount;
    private List<CategorySummary> categoryBreakdown;
    private List<MonthlySummary> monthlyBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String categoryName;
        private BigDecimal totalAmount;
        private Long count;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySummary {
        private String month;
        private BigDecimal totalAmount;
        private Long count;
    }
}
