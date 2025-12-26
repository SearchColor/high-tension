package com.high.user.infrastructure.runner;

import com.high.user.infrastructure.persistence.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * User Service Warmup Runner
 *
 * 애플리케이션 시작 시 주요 컴포넌트를 자동으로 초기화합니다.
 * Cold Start 문제를 방지하여 첫 요청 지연을 최소화합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarmupRunner implements ApplicationRunner {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JpaUserRepository jpaUserRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== User Service Warmup 시작 ===");
        long startTime = System.currentTimeMillis();

        try {
            // Redis 연결 초기화
            warmupRedis();

            // Database/JPA 초기화
            warmupDatabase();

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("=== User Service Warmup 완료 ({}ms) ===", elapsed);

        } catch (Exception e) {
            log.error("User Service Warmup 실패", e);
        }
    }

    private void warmupRedis() {
        try {
            redisTemplate.opsForValue().get("warmup:user:init");
            log.info("Redis 연결 초기화 완료");
        } catch (Exception e) {
            log.warn("Redis 연결 초기화 실패: {}", e.getMessage());
        }
    }

    private void warmupDatabase() {
        try {
            long count = jpaUserRepository.count();
            log.info("Database/JPA 초기화 완료 (user count: {})", count);
        } catch (Exception e) {
            log.warn("Database/JPA 초기화 실패: {}", e.getMessage());
        }
    }
}