package com.taingy.expensetracker.mapper;

import com.taingy.expensetracker.dto.AuditLogResponse;
import com.taingy.expensetracker.model.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toDto(AuditLog auditLog) {
        if (auditLog == null) return null;

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .method(auditLog.getMethod())
                .endpoint(auditLog.getEndpoint())
                .userName(auditLog.getUsername())
                .details(auditLog.getRequestBody())
                .ipAddress(auditLog.getIpAddress())
                .success(auditLog.getSuccess())
                .errorMessage(auditLog.getErrorMessage())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
