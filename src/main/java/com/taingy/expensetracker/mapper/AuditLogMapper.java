package com.taingy.expensetracker.mapper;

import com.taingy.expensetracker.dto.AuditLogResponse;
import com.taingy.expensetracker.model.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toDto(AuditLog auditLog) {
        if (auditLog == null) return null;

        String userName = null;
        if (auditLog.getUser() != null) {
            userName = auditLog.getUser().getFirstName() + " " + auditLog.getUser().getLastName();
        }

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .userEmail(auditLog.getUserEmail())
                .userName(userName)
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .success(auditLog.getSuccess())
                .errorMessage(auditLog.getErrorMessage())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
