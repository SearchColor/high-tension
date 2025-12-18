package com.high.user.infrastructure.persistence;

import com.high.user.domain.entity.Passkey;
import com.high.user.domain.repository.PasskeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasskeyRepositoryAdaptor implements PasskeyRepository {

    private final JpaPasskeyRepository jpaPasskeyRepository;

    @Override
    public Passkey save(Passkey passkey) {
        return jpaPasskeyRepository.save(passkey);
    }

    @Override
    public Optional<Passkey> findById(UUID passkeyId) {
        return jpaPasskeyRepository.findById(passkeyId);
    }

    @Override
    public Optional<Passkey> findByIdAndDeletedAtIsNull(UUID passkeyId) {
        return jpaPasskeyRepository.findByPasskeyIdAndDeletedAtIsNull(passkeyId);
    }

    @Override
    public Optional<Passkey> findByCredentialId(String credentialId) {
        return jpaPasskeyRepository.findByCredentialIdAndDeletedAtIsNull(credentialId);
    }

    @Override
    public List<Passkey> findByUserIdAndDeletedAtIsNull(UUID userId) {
        return jpaPasskeyRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public boolean existsByCredentialId(String credentialId) {
        return jpaPasskeyRepository.existsByCredentialId(credentialId);
    }

    @Override
    public void deleteById(UUID passkeyId) {
        jpaPasskeyRepository.deleteById(passkeyId);
    }
}