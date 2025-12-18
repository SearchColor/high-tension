package com.high.user.domain.entity;

import com.high.user.domain.exception.InvalidChallengeDataException;
import com.high.user.domain.vo.ChallengeOperation;
import com.library.jpa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_passkey_challenges")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PasskeyChallenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "challenge_id")
    private UUID challengeId;

    @Column(name = "challenge", nullable = false, unique = true, length = 512)
    private String challenge;  // Base64URL encoded

    @Column(name = "user_id")
    private UUID userId;  // null for authentication (username-less)

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 20)
    private ChallengeOperation operation;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;  // JSON serialized request object

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Private constructor
    private PasskeyChallenge(String challenge, UUID userId, ChallengeOperation operation,
                             String requestData, LocalDateTime expiresAt) {
        this.challenge = challenge;
        this.userId = userId;
        this.operation = operation;
        this.requestData = requestData;
        this.expiresAt = expiresAt;
    }

    // Factory method
    public static PasskeyChallenge createForRegistration(String challenge, UUID userId,
                                                          String requestData, int ttlMinutes) {
        if (challenge == null || challenge.isBlank()) {
            throw new InvalidChallengeDataException();
        }
        if (userId == null) {
            throw new InvalidChallengeDataException();
        }
        if (requestData == null || requestData.isBlank()) {
            throw new InvalidChallengeDataException();
        }
        return new PasskeyChallenge(
            challenge, userId, ChallengeOperation.REGISTRATION,
            requestData, LocalDateTime.now().plusMinutes(ttlMinutes)
        );
    }

    public static PasskeyChallenge createForAuthentication(String challenge, UUID userId,
                                                            String requestData, int ttlMinutes) {
        if (challenge == null || challenge.isBlank()) {
            throw new InvalidChallengeDataException();
        }
        // userId는 NULL 허용 (username-less 인증)
        if (requestData == null || requestData.isBlank()) {
            throw new InvalidChallengeDataException();
        }
        return new PasskeyChallenge(
            challenge, userId, ChallengeOperation.AUTHENTICATION,
            requestData, LocalDateTime.now().plusMinutes(ttlMinutes)
        );
    }

    // Query methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}