package com.taingy.expensetracker.controller;

import com.taingy.expensetracker.dto.ResetPasswordRequest;
import com.taingy.expensetracker.dto.ResponseMessage;
import com.taingy.expensetracker.dto.UserRequest;
import com.taingy.expensetracker.dto.UserResponse;
import com.taingy.expensetracker.mapper.UserMapper;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) UUID roleId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(userService.findUsersWithFilters(searchTerm, roleId, isActive, isVerified, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        User user = userService.getUserById(id).orElseThrow(
                () -> new IllegalArgumentException("User with id " + id + " not found"));

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest) {
        UserResponse response = userService.createUser(userRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody UserRequest userRequest) {
        UserResponse response = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> deleteUser(@PathVariable UUID id) {
        User user = userService.getUserById(id).orElseThrow(
                () -> new IllegalArgumentException("User with id " + id + " not found"));

        userService.deleteUser(user.getId());

        return ResponseEntity.ok(new ResponseMessage("Successfully deleted User with id " + id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<ResponseMessage> resetPassword(
            @PathVariable UUID id,
            @RequestBody ResetPasswordRequest request) {

        userService.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));

        userService.resetPassword(id, request.getNewPassword());

        return ResponseEntity.ok(new ResponseMessage("Password successfully reset for user with id " + id));
    }
}
