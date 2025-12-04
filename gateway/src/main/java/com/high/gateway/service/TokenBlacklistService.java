package com.high.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Token Blacklist Service
 * Redis에서 블랙리스트 토큰 확인 (User Service의 RefreshTokenService와 동일한 키 패턴 사용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    /**
     * 토큰이 블랙리스트에 등록되어 있는지 확인
     * @param token JWT 토큰
     * @return 블랙리스트 등록 여부 (Mono<Boolean>)
     */
    public Mono<Boolean> isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return reactiveRedisTemplate.hasKey(key)
                .doOnNext(exists -> {
                    if (exists) {
                        log.warn("Blacklisted token detected: {}", token.substring(0, Math.min(20, token.length())));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error checking blacklist for token: {}", e.getMessage());
                    // Redis 연결 오류 시 안전을 위해 true 반환 (접근 거부)
                    return Mono.just(true);
                });
    }
}