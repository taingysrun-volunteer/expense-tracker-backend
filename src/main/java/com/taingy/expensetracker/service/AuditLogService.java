package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.AuditLogResponse;
import com.taingy.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditLogService {

    void log(String action, String entityType, String entityId, User user, String details, 
             String ipAddress, String userAgent, Boolean success, String errorMessage);

    Page<AuditLogResponse> findAll(Pageable pageable);

    Page<AuditLogResponse> findByUserId(UUID userId, Pageable pageable);

    Page<AuditLogResponse> findByAction(String action, Pageable pageable);

    Page<AuditLogResponse> findByEntityType(String entityType, Pageable pageable);

    Page<AuditLogResponse> findByEntityId(String entityId, Pageable pageable);

    Page<AuditLogResponse> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<AuditLogResponse> findByUserIdAndDateRange(UUID userId, LocalDateTime startDate, 
                                                      LocalDateTime endDate, Pageable pageable);

    List<AuditLogResponse> getRecentActivityByUserId(UUID userId);
}
