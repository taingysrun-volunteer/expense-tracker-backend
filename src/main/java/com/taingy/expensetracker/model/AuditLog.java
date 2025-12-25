package com.taingy.expensetracker.model;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Data
@Table(name = "audit_logs")
public class AuditLog {


    @Id
    @GeneratedValue
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @Column(nullable = false, length = 255)
    private String action;


    @Column(nullable = false, length = 255)
    private String entity;


    @Column(name = "entity_id")
    private UUID entityId;


    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

}
