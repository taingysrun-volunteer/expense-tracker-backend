package com.taingy.expensetracker.service.impl;

import com.taingy.expensetracker.model.Role;
import com.taingy.expensetracker.repository.RoleRepository;
import com.taingy.expensetracker.service.RoleService;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;


@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
