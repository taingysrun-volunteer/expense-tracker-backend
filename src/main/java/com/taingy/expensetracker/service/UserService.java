package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.UserRequest;
import com.taingy.expensetracker.dto.UserResponse;
import com.taingy.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Optional<User> getUserById(UUID id);
    UserResponse getUserByEmail(String email);
    Page<UserResponse> findUsersWithFilters(String searchTerm, UUID roleId, Boolean isActive, Boolean isVerified, Pageable pageable);
    UserResponse createUser(UserRequest userRequest);
    void deactivateUser(UUID id);
    void deleteUser(UUID id);
}
