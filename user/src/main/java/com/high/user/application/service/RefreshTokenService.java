package com.high.user.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Refresh Token Redis 저장/조회/삭제 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.security.jwt.refresh-token-validity}")
    private Long refreshTokenValidity;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    /**
     * Refresh Token을 Redis에 저장
     * @param userId 사용자 ID
     * @param refreshToken Refresh Token
     */
    public void saveRefreshToken(UUID userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId.toString();

        redisTemplate.opsForValue().set(
            key,
            refreshToken,
            refreshTokenValidity,
            TimeUnit.MILLISECONDS
        );

        log.debug("Refresh token saved for user: {}", userId);
    }

    /**
     * Redis에서 Refresh Token 조회
     * @param userId 사용자 ID
     * @return Refresh Token (없으면 null)
     */
    public String getRefreshToken(UUID userId) {
        String key = REFRESH_TOKEN_PREFIX + userId.toString();
        String token = redisTemplate.opsForValue().get(key);

        log.debug("Refresh token retrieved for user: {}, exists: {}", userId, token != null);
        return token;
    }

    /**
     * Redis에서 Refresh Token 삭제 (로그아웃 시 사용)
     * @param userId 사용자 ID
     */
    public void deleteRefreshToken(UUID userId) {
        String key = REFRESH_TOKEN_PREFIX + userId.toString();
        Boolean deleted = redisTemplate.delete(key);

        log.debug("Refresh token deleted for user: {}, success: {}", userId, deleted);
    }

    /**
     * Refresh Token 검증 (저장된 토큰과 일치하는지 확인)
     * @param userId 사용자 ID
     * @param refreshToken 검증할 Refresh Token
     * @return 일치 여부
     */
    public boolean validateRefreshToken(UUID userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);

        if (storedToken == null) {
            log.warn("No refresh token found for user: {}", userId);
            return false;
        }

        boolean isValid = storedToken.equals(refreshToken);
        log.debug("Refresh token validation for user: {}, valid: {}", userId, isValid);

        return isValid;
    }
}
