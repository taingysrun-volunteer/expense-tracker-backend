package com.taingy.expensetracker.controller;

import com.taingy.expensetracker.dto.AuditLogResponse;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.repository.UserRepository;
import com.taingy.expensetracker.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @Autowired
    public AuditLogController(AuditLogService auditLogService, UserRepository userRepository) {
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> findAll(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // If user has USER role, restrict to their own audit logs only
        UUID effectiveUserId = userId;
        if ("USER".equals(currentUser.getRole().getName())) {
            effectiveUserId = currentUser.getId();
        }

        // Apply filters
        Page<AuditLogResponse> result;

        if (startDate != null && endDate != null) {
            if (effectiveUserId != null) {
                result = auditLogService.findByUserIdAndDateRange(effectiveUserId, startDate, endDate, pageable);
            } else {
                result = auditLogService.findByDateRange(startDate, endDate, pageable);
            }
        } else if (effectiveUserId != null) {
            result = auditLogService.findByUserId(effectiveUserId, pageable);
        } else if (action != null) {
            result = auditLogService.findByAction(action, pageable);
        } else if (entityType != null) {
            result = auditLogService.findByEntityType(entityType, pageable);
        } else if (entityId != null) {
            result = auditLogService.findByEntityId(entityId, pageable);
        } else {
            result = auditLogService.findAll(pageable);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AuditLogResponse>> getRecentActivity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        List<AuditLogResponse> recentActivity = auditLogService.getRecentActivityByUserId(currentUser.getId());
        return ResponseEntity.ok(recentActivity);
    }
}
