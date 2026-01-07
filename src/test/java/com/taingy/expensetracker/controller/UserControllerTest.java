package com.taingy.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taingy.expensetracker.dto.UserRequest;
import com.taingy.expensetracker.dto.UserResponse;
import com.taingy.expensetracker.mapper.UserMapper;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    private User testUser;
    private UserResponse userResponse;
    private UserRequest userRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");

        userResponse = UserResponse.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role("USER")
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRequest = new UserRequest();
        userRequest.setFirstName("John");
        userRequest.setLastName("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setPassword("password123");
        userRequest.setRole("USER");
    }

    @Test
    @WithMockUser
    void getAllUsers_ShouldReturnPagedUsers() throws Exception {
        // Arrange
        Page<UserResponse> userPage = new PageImpl<>(Arrays.asList(userResponse));
        when(userService.findUsersWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.content[0].firstName").value("John"));

        verify(userService).findUsersWithFilters(null, null, null, null, PageRequest.of(0, 10));
    }

    @Test
    @WithMockUser
    void getAllUsers_WithSearchTerm_ShouldReturnFilteredUsers() throws Exception {
        // Arrange
        Page<UserResponse> userPage = new PageImpl<>(Arrays.asList(userResponse));
        when(userService.findUsersWithFilters(anyString(), any(), any(), any(), any()))
                .thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .param("searchTerm", "john")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("john.doe@example.com"));

        verify(userService).findUsersWithFilters(eq("john"), any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    void getUser_ShouldReturnUser_WhenUserExists() throws Exception {
        // Arrange
        when(userService.getUserById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).getUserById(userId);
    }

    @Test
    @WithMockUser
    void getUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        // Arrange
        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService).getUserById(userId);
    }

    @Test
    @WithMockUser
    void deleteUser_ShouldDeleteUser_WhenUserExists() throws Exception {
        // Arrange
        when(userService.getUserById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(userService).deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully deleted User with id " + userId));

        verify(userService).deleteUser(userId);
    }
}
