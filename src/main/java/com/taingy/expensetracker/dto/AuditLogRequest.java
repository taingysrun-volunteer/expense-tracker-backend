package com.taingy.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequest {

    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private Boolean success;
    private String errorMessage;
}
