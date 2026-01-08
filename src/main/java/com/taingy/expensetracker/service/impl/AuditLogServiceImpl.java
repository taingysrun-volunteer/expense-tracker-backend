package com.taingy.expensetracker.service.impl;

import com.taingy.expensetracker.dto.AuditLogResponse;
import com.taingy.expensetracker.mapper.AuditLogMapper;
import com.taingy.expensetracker.model.AuditLog;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.repository.AuditLogRepository;
import com.taingy.expensetracker.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Autowired
    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    @Transactional
    public void log(String action, String entityType, String entityId, User user, String details,
                    String ipAddress, String userAgent, Boolean success, String errorMessage) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .user(user)
                .userEmail(user != null ? user.getEmail() : "system")
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success != null ? success : true)
                .errorMessage(errorMessage)
                .build();

        auditLogRepository.save(auditLog);
    }

    @Override
    public Page<AuditLogResponse> findAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogResponse> findByUserId(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogResponse> findByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogResponse> findByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogResponse> findByEntityId(String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityIdOrderByCreatedAtDesc(entityId, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogResponse> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public Page<AuditLogResponse> findByUserIdAndDateRange(UUID userId, LocalDateTime startDate,
                                                             LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable)
                .map(auditLogMapper::toDto);
    }

    @Override
    public List<AuditLogResponse> getRecentActivityByUserId(UUID userId) {
        return auditLogRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(auditLogMapper::toDto)
                .collect(Collectors.toList());
    }
}
