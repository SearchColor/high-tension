package com.high.user.infrastructure.persistence;

import com.high.user.domain.entity.User;
import com.high.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdaptor implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return jpaUserRepository.findById(userId);
    }

    @Override
    public Optional<User> findByIdAndDeletedAtIsNull(UUID userId) {
        return jpaUserRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByEmailAndDeletedAtIsNull(String email) {
        return jpaUserRepository.findByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public Page<User> findByEmailContainingAndDeletedAtIsNull(String email, Pageable pageable) {
        return jpaUserRepository.findByEmailContainingAndDeletedAtIsNull(email, pageable);
    }

    @Override
    public List<User> findByUserIdInAndDeletedAtIsNull(List<UUID> userIds) {
        return jpaUserRepository.findByUserIdInAndDeletedAtIsNull(userIds);
    }
}