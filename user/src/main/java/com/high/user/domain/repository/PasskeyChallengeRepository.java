package com.high.user.domain.repository;

import com.high.user.domain.entity.PasskeyChallenge;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasskeyChallengeRepository {

    PasskeyChallenge save(PasskeyChallenge challenge);

    Optional<PasskeyChallenge> findByChallenge(String challenge);

    void deleteByChallenge(String challenge);

    void deleteExpiredChallenges(LocalDateTime now);
}