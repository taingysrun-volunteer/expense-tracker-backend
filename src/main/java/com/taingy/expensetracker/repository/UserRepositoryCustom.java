package com.taingy.expensetracker.repository;

import com.taingy.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserRepositoryCustom {

    Page<User> findUsersWithFilters(
            String searchTerm,
            UUID roleId,
            Boolean isActive,
            Boolean isVerified,
            Pageable pageable
    );
}
