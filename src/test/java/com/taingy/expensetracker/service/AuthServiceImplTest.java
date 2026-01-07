package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.*;
import com.taingy.expensetracker.mapper.UserMapper;
import com.taingy.expensetracker.model.Role;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.repository.RoleRepository;
import com.taingy.expensetracker.repository.UserRepository;
import com.taingy.expensetracker.security.JwtUtil;
import com.taingy.expensetracker.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role userRole;
    private UserRequest userRequest;
    private UserResponse userResponse;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();

        userRole = new Role();
        userRole.setId(1);
        userRole.setName("USER");
        userRole.setDescription("Regular user");

        testUser = new User();
        testUser.setId(userId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("$2a$10$encodedPassword"); // Encoded password
        testUser.setRole(userRole);
        testUser.setIsActive(true);
        testUser.setIsVerified(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

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

        loginRequest = new LoginRequest("john.doe@example.com", "password123");
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        // Act
        AuthResponse result = authService.login(loginRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user()).isNotNull();
        assertThat(result.user().getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
        verify(jwtUtil).generateToken("john.doe@example.com", "USER");
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username or password");

        verify(userRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username or password");

        verify(userRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_ShouldThrowException_WhenUserIsNotVerified() {
        // Arrange
        testUser.setIsVerified(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Please verify your email before logging in. Check your email for the verification code.");

        verify(userRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void register_ShouldRegisterUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRequest.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ResponseMessage result = authService.register(userRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.message()).contains("Registration successful");
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(userRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already in use");

        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldEncodePassword() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRequest.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.register(userRequest);

        // Assert
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("$2a$10$encodedPassword")
        ));
    }

    @Test
    void register_ShouldSetUserRoleToUSER() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRequest.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.register(userRequest);

        // Assert
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(argThat(user ->
                user.getRole() != null && user.getRole().getName().equals("USER")
        ));
    }
}
