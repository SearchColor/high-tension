package com.high.gateway.runner;

import com.high.gateway.service.TokenBucketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Gateway Warmup Runner
 *
 * 애플리케이션 시작 시 주요 컴포넌트를 자동으로 초기화합니다.
 * Cold Start 문제를 방지하여 첫 요청 지연을 최소화합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarmupRunner implements ApplicationRunner {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final TokenBucketService tokenBucketService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== Gateway Warmup 시작 ===");
        long startTime = System.currentTimeMillis();

        try {
            // Redis 연결 초기화
            warmupRedis();

            // TokenBucket 서비스 초기화
            warmupTokenBucket();

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("=== Gateway Warmup 완료 ({}ms) ===", elapsed);

        } catch (Exception e) {
            log.error("Gateway Warmup 실패", e);
        }
    }

    private void warmupRedis() {
        try {
            reactiveRedisTemplate.opsForValue()
                .get("warmup:gateway:init")
                .block();
            log.info("Redis 연결 초기화 완료");
        } catch (Exception e) {
            log.warn("Redis 연결 초기화 실패: {}", e.getMessage());
        }
    }

    private void warmupTokenBucket() {
        try {
            tokenBucketService.tryConsume("warmup:gateway:init", 1)
                .block();
            log.info("TokenBucket 서비스 초기화 완료");
        } catch (Exception e) {
            log.warn("TokenBucket 서비스 초기화 실패: {}", e.getMessage());
        }
    }
}