package com.taingy.expensetracker.service.impl;

import com.taingy.expensetracker.dto.AuditLogResponse;
import com.taingy.expensetracker.mapper.AuditLogMapper;
import com.taingy.expensetracker.model.AuditLog;
import com.taingy.expensetracker.repository.AuditLogRepository;
import com.taingy.expensetracker.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    public void log(String action, String entityType, String entityId, String details,
                    String ipAddress, String userAgent, Boolean success, String errorMessage) {
        AuditLog auditLog = AuditLog.builder()
                .method(action)
                .requestBody(details)
                .ipAddress(ipAddress)
                .success(success != null ? success : true)
                .errorMessage(errorMessage)
                .build();

        auditLogRepository.save(auditLog);
    }


    @Override
    public Page<AuditLogResponse> findAllWithFilters( String username, LocalDateTime startDate, LocalDateTime endDate,Pageable pageable) {
        username = "%" + username + "%";
        return auditLogRepository.findWithFilters(username, startDate, endDate, pageable)
                .map(auditLogMapper::toDto);
    }

}
