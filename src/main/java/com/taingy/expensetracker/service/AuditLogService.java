package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditLogService {
    void log(String action, String entityType, String entityId, String details,
             String ipAddress, String userAgent, Boolean success, String errorMessage);
    Page<AuditLogResponse> findAllWithFilters(String username, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
