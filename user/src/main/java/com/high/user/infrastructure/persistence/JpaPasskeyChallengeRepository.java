package com.high.user.infrastructure.persistence;

import com.high.user.domain.entity.PasskeyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaPasskeyChallengeRepository extends JpaRepository<PasskeyChallenge, UUID> {

    Optional<PasskeyChallenge> findByChallenge(String challenge);

    void deleteByChallenge(String challenge);

    @Modifying
    @Query("DELETE FROM PasskeyChallenge c WHERE c.expiresAt < :now")
    void deleteByExpiresAtBefore(@Param("now") LocalDateTime now);
}