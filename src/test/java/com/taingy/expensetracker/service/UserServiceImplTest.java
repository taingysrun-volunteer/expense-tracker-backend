package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.UserRequest;
import com.taingy.expensetracker.dto.UserResponse;
import com.taingy.expensetracker.mapper.UserMapper;
import com.taingy.expensetracker.model.Role;
import com.taingy.expensetracker.model.User;
import com.taingy.expensetracker.repository.RoleRepository;
import com.taingy.expensetracker.repository.UserRepository;
import com.taingy.expensetracker.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role testRole;
    private UserRequest userRequest;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testRole = new Role();
        testRole.setId(1);
        testRole.setName("USER");
        testRole.setDescription("Regular user");

        testUser = new User();
        testUser.setId(userId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(testRole);
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
    }

    @Test
    void createUser_ShouldCreateUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserRequest.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        // Act
        UserResponse result = userService.createUser(userRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already in use");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserById(userId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById(userId);

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserByEmail_ShouldReturnUserResponse_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getUserByEmail("john.doe@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void findUsersWithFilters_ShouldReturnPagedUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));

        when(userRepository.findUsersWithFilters(anyString(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(userPage);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        // Act
        Page<UserResponse> result = userService.findUsersWithFilters("john", null, true, true, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository).findUsersWithFilters("john", null, true, true, pageable);
    }

    @Test
    void deactivateUser_ShouldDeactivateUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.deactivateUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
        assertThat(testUser.getIsActive()).isFalse();
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_ShouldDoNothing_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
}
