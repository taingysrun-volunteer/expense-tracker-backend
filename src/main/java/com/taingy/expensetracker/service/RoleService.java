package com.taingy.expensetracker.service;

import com.taingy.expensetracker.model.Role;

import java.util.List;
import java.util.Optional;


public interface RoleService {
    Role createRole(Role role);
    Optional<Role> getRoleByName(String name);
    List<Role> getAllRoles();
}
