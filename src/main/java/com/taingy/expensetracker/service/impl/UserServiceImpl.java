package com.taingy.expensetracker.service.impl;

import com.taingy.expensetracker.dto.UserRequest;
import com.taingy.expensetracker.dto.UserResponse;
import com.taingy.expensetracker.mapper.UserMapper;
import com.taingy.expensetracker.model.Role;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.repository.RoleRepository;
import com.taingy.expensetracker.repository.UserRepository;
import com.taingy.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsVerified(true);

        Role role = roleRepository.findByName(request.getRole()).get();
        user.setRole(role);
        user = userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Override
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        return userMapper.toDto(user);
    }

    @Override
    public org.springframework.data.domain.Page<UserResponse> findUsersWithFilters(String searchTerm, UUID roleId, Boolean isActive, Boolean isVerified, org.springframework.data.domain.Pageable pageable) {
        return userRepository.findUsersWithFilters(searchTerm, roleId, isActive, isVerified, pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
    }
}
