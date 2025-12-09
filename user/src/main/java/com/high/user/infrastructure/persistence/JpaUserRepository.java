package com.high.user.infrastructure.persistence;

import com.high.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID> {

    // Find by ID with soft delete check
    Optional<User> findByUserIdAndDeletedAtIsNull(UUID userId);

    // Find by email
    Optional<User> findByEmail(String email);

    // Find by email with soft delete check
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // Check email existence
    boolean existsByEmail(String email);

    // Search by email (partial match)
    Page<User> findByEmailContainingAndDeletedAtIsNull(String email, Pageable pageable);
}