package com.taingy.expensetracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 50)
    private String method;

    @Column(nullable = false)
    private String endpoint;

    @Column(length = 100)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    @Builder.Default
    private Boolean success = true;

    @Column(length = 500)
    private String errorMessage;

    @Column
    private int status;

    @Column
    private long executionTimeMs;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
