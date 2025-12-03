package com.high.user.domain.repository;

import com.high.user.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    // Save
    User save(User user);

    // Find by ID
    Optional<User> findById(UUID userId);

    // Find by ID with soft delete check
    Optional<User> findByIdAndDeletedAtIsNull(UUID userId);

    // Find by email
    Optional<User> findByEmail(String email);

    // Find by email with soft delete check
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // Check email existence
    boolean existsByEmail(String email);
}