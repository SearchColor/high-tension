package com.high.user.infrastructure.persistence;

import com.high.user.domain.entity.Passkey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaPasskeyRepository extends JpaRepository<Passkey, UUID> {

    Optional<Passkey> findByPasskeyIdAndDeletedAtIsNull(UUID passkeyId);

    Optional<Passkey> findByCredentialIdAndDeletedAtIsNull(String credentialId);

    List<Passkey> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByCredentialId(String credentialId);

    List<Passkey> findByDeletedAtBefore(java.time.LocalDateTime deletedAt);
}