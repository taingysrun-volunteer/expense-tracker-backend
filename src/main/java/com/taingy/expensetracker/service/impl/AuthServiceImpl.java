package com.taingy.expensetracker.service.impl;

import com.taingy.expensetracker.dto.*;
import com.taingy.expensetracker.mapper.UserMapper;
import com.taingy.expensetracker.model.Role;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.repository.RoleRepository;
import com.taingy.expensetracker.repository.UserRepository;
import com.taingy.expensetracker.security.JwtUtil;
import com.taingy.expensetracker.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserMapper userMapper, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.roleRepository = roleRepository;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (!user.getIsVerified()) {
            throw new IllegalArgumentException("Please verify your email before logging in. Check your email for the verification code.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
        return new AuthResponse(token, userMapper.toDto(user));
    }

    @Override
    public ResponseMessage register(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsVerified(true);

        Role role = roleRepository.findByName("USER").get();
        user.setRole(role);
        userRepository.save(user);

        return new ResponseMessage(
                "Registration successful. Please check your email for the verification code."
        );
    }
}
