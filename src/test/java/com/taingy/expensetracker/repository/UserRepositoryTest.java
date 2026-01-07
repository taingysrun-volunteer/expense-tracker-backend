package com.taingy.expensetracker.repository;

import com.taingy.expensetracker.model.Role;
import com.taingy.expensetracker.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role userRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create and persist role
        userRole = new Role();
        userRole.setId(1);
        userRole.setName("USER");
        userRole.setDescription("Regular user");
        entityManager.persist(userRole);

        // Create and persist test user
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password");
        testUser.setRole(userRole);
        testUser.setIsActive(true);
        testUser.setIsVerified(true);
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // Act
        Optional<User> found = userRepository.findByEmail("john.doe@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Act
        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void findUsersWithFilters_WithSearchTerm_ShouldReturnMatchingUsers() {
        // Act
        Page<User> result = userRepository.findUsersWithFilters(
                "john", null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void findUsersWithFilters_WithActiveFilter_ShouldReturnActiveUsers() {
        // Arrange
        User inactiveUser = new User();
        inactiveUser.setFirstName("Jane");
        inactiveUser.setLastName("Smith");
        inactiveUser.setEmail("jane.smith@example.com");
        inactiveUser.setPassword("password");
        inactiveUser.setRole(userRole);
        inactiveUser.setIsActive(false);
        inactiveUser.setIsVerified(true);
        entityManager.persist(inactiveUser);
        entityManager.flush();

        // Act
        Page<User> result = userRepository.findUsersWithFilters(
                null, null, true, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIsActive()).isTrue();
    }

    @Test
    void findUsersWithFilters_WithNoFilters_ShouldReturnAllUsers() {
        // Act
        Page<User> result = userRepository.findUsersWithFilters(
                null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findUsersWithFilters_WithEmailSearch_ShouldReturnMatchingUsers() {
        // Act
        Page<User> result = userRepository.findUsersWithFilters(
                "doe", null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).contains("doe");
    }
}
