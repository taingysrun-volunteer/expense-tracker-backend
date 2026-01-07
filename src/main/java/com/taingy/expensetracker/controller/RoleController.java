package com.taingy.expensetracker.controller;

import com.taingy.expensetracker.dto.ListResponse;
import com.taingy.expensetracker.model.Role;
import com.taingy.expensetracker.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<ListResponse<Role>> getRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(new ListResponse<>(roles, roles.size()));
    }
}
