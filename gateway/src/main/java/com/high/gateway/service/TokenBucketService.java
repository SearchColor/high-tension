package com.high.gateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.gateway.config.RateLimitConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Token Bucket Rate Limiting Service
 *
 * Redis Lua Script를 실행하여 Token Bucket 알고리즘을 구현하고
 * 분산 환경에서 원자적 연산을 보장합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBucketService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final RateLimitConfig rateLimitConfig;

    private RedisScript<Long> tokenBucketScript;

    /**
     * 애플리케이션 시작 시 Lua 스크립트 로드
     */
    @PostConstruct
    public void init() {
        try {
            Resource resource = new ClassPathResource("lua/token_bucket.lua");
            String scriptContent = StreamUtils.copyToString(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8
            );
            this.tokenBucketScript = RedisScript.of(scriptContent, Long.class);
            log.info("Token bucket Lua script loaded successfully");
        } catch (IOException e) {
            log.error("Failed to load token bucket Lua script", e);
            throw new RuntimeException("Failed to load token bucket Lua script", e);
        }
    }

    /**
     * 버킷에서 토큰 소비 시도
     *
     * @param userId 사용자 ID (Rate Limiting 키)
     * @param tokens 소비할 토큰 수 (일반적으로 1)
     * @return Mono<Boolean> - 토큰이 있어서 소비 성공 시 true, Rate Limit 초과 시 false
     */
    public Mono<Boolean> tryConsume(String userId, int tokens) {
        String key = "rate_limit:user:" + userId;
        long now = System.currentTimeMillis();

        List<String> keys = Collections.singletonList(key);
        List<String> args = Arrays.asList(
                String.valueOf(rateLimitConfig.getMaxTokens()),     // max_tokens (100)
                String.valueOf(rateLimitConfig.getRefillRate()),    // refill_rate (10)
                String.valueOf(tokens),                             // requested_tokens (1)
                String.valueOf(now)                                 // current_timestamp_millis
        );

        return reactiveRedisTemplate.execute(tokenBucketScript, keys, args)
                .next()
                .map(result -> {
                    // 스크립트는 실패 시 -1, 성공 시 >= 0 (남은 토큰 수) 반환
                    boolean allowed = result >= 0;
                    if (!allowed) {
                        log.warn("Rate limit exceeded for user: {}", userId);
                    } else {
                        log.debug("Rate limit check passed for user: {}, remaining tokens: {}", userId, result);
                    }
                    return allowed;
                })
                .onErrorResume(e -> {
                    log.error("Error executing token bucket script for user: {}", userId, e);
                    // Fail-closed: Redis 오류 시 안전을 위해 요청 거부
                    return Mono.just(false);
                })
                .defaultIfEmpty(false);  // 결과 없으면 기본값 false
    }

    /**
     * 사용자의 남은 토큰 수 조회
     *
     * 저장된 버킷 상태에 리필 로직을 적용하여 현재 토큰 수를 계산합니다.
     *
     * @param userId 사용자 ID (Rate Limiting 키)
     * @return Mono<Integer> - 현재 사용 가능한 토큰 수
     */
    public Mono<Integer> getRemainingTokens(String userId) {
        String key = "rate_limit:user:" + userId;

        return reactiveRedisTemplate.opsForValue().get(key)
                .map(json -> {
                    try {
                        // JSON 버킷 상태 파싱
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode node = mapper.readTree(json);
                        int tokens = node.get("tokens").asInt();
                        long lastRefill = node.get("lastRefillTimestamp").asLong();

                        // 경과 시간 기반 리필된 토큰 계산
                        long now = System.currentTimeMillis();
                        long elapsedSeconds = (now - lastRefill) / 1000;
                        int refilled = (int) Math.floor(elapsedSeconds * rateLimitConfig.getRefillRate());

                        // 현재 토큰 수 반환 (최대값으로 제한)
                        int currentTokens = Math.min(rateLimitConfig.getMaxTokens(), tokens + refilled);
                        log.debug("Remaining tokens for user {}: {}", userId, currentTokens);
                        return currentTokens;
                    } catch (Exception e) {
                        log.error("Error parsing bucket state for user: {}", userId, e);
                        return rateLimitConfig.getMaxTokens();  // 오류 시 최대값 반환
                    }
                })
                .defaultIfEmpty(rateLimitConfig.getMaxTokens())  // 빈 버킷 = 최대 용량
                .onErrorReturn(rateLimitConfig.getMaxTokens());  // 오류 시 최대값 반환
    }
}