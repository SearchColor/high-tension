package com.high.user.domain.repository;

import com.high.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
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

    // Search by email (partial match)
    Page<User> findByEmailContainingAndDeletedAtIsNull(String email, Pageable pageable);

    // Find by IDs (Batch query for Internal API)
    List<User> findByUserIdInAndDeletedAtIsNull(List<UUID> userIds);
}