package com.taingy.expensetracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "roles")
public class Role {


    @Id
    private Integer id;


    @Column(nullable = false, unique = true, length = 100)
    private String name;


    @Column(length = 255)
    private String description;

}
