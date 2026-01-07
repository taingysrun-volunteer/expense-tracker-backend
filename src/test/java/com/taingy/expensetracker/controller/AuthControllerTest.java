package com.taingy.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taingy.expensetracker.dto.*;
import com.taingy.expensetracker.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private LoginRequest loginRequest;
    private UserRequest userRequest;
    private AuthResponse authResponse;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();

        loginRequest = new LoginRequest("john.doe@example.com", "password123");

        userRequest = new UserRequest();
        userRequest.setFirstName("John");
        userRequest.setLastName("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setPassword("password123");
        userRequest.setRole("USER");

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

        authResponse = new AuthResponse("jwt-token", userResponse);
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("John"))
                .andExpect(jsonPath("$.user.lastName").value("Doe"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldReturnError_WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid username or password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldReturnError_WhenUserNotVerified() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Please verify your email before logging in. Check your email for the verification code."));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void register_ShouldReturnSuccessMessage_WhenRegistrationIsSuccessful() throws Exception {
        // Arrange
        ResponseMessage responseMessage = new ResponseMessage("Registration successful. Please check your email for the verification code.");
        when(authService.register(any(UserRequest.class))).thenReturn(responseMessage);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration successful. Please check your email for the verification code."));

        verify(authService).register(any(UserRequest.class));
    }

    @Test
    void register_ShouldReturnError_WhenEmailAlreadyExists() throws Exception {
        // Arrange
        when(authService.register(any(UserRequest.class)))
                .thenThrow(new IllegalArgumentException("Email is already in use"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService).register(any(UserRequest.class));
    }

    @Test
    void register_ShouldAcceptAllRequiredFields() throws Exception {
        // Arrange
        ResponseMessage responseMessage = new ResponseMessage("Registration successful. Please check your email for the verification code.");
        when(authService.register(any(UserRequest.class))).thenReturn(responseMessage);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());

        verify(authService).register(argThat(request ->
                request.getEmail().equals("john.doe@example.com") &&
                        request.getFirstName().equals("John") &&
                        request.getLastName().equals("Doe") &&
                        request.getPassword().equals("password123")
        ));
    }
}
