package com.taingy.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID id;
    private String action;
    private String entityType;
    private String entityId;
    private String userEmail;
    private String userName;
    private String details;
    private String ipAddress;
    private String userAgent;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime createdAt;
}
