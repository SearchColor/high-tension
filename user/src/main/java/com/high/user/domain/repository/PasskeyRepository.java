package com.high.user.domain.repository;

import com.high.user.domain.entity.Passkey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasskeyRepository {

    Passkey save(Passkey passkey);

    Optional<Passkey> findById(UUID passkeyId);

    Optional<Passkey> findByIdAndDeletedAtIsNull(UUID passkeyId);

    Optional<Passkey> findByCredentialId(String credentialId);

    List<Passkey> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByCredentialId(String credentialId);

    void deleteById(UUID passkeyId);
}