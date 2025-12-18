package com.high.user.infrastructure.persistence;

import com.high.user.domain.entity.PasskeyChallenge;
import com.high.user.domain.repository.PasskeyChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasskeyChallengeRepositoryAdaptor implements PasskeyChallengeRepository {

    private final JpaPasskeyChallengeRepository jpaRepository;

    @Override
    public PasskeyChallenge save(PasskeyChallenge challenge) {
        return jpaRepository.save(challenge);
    }

    @Override
    public Optional<PasskeyChallenge> findByChallenge(String challenge) {
        return jpaRepository.findByChallenge(challenge);
    }

    @Override
    @Transactional
    public void deleteByChallenge(String challenge) {
        jpaRepository.deleteByChallenge(challenge);
    }

    @Override
    @Transactional
    public void deleteExpiredChallenges(LocalDateTime now) {
        jpaRepository.deleteByExpiresAtBefore(now);
    }
}