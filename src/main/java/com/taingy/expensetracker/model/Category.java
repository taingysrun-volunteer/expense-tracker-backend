package com.taingy.expensetracker.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;


@Entity
@Data
@Table(name = "categories")
public class Category {


    @Id
    @GeneratedValue
    private UUID id;


    @Column(nullable = false, unique = true, length = 100)
    private String name;


    @Column(length = 255)
    private String description;


    @Column(nullable = false)
    private Boolean isActive = true;

}
