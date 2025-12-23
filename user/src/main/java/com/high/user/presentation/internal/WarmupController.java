package com.high.user.presentation.internal;

import com.high.user.infrastructure.persistence.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * User Service Warmup Controller
 *
 * 서비스 재시작 후 Cold Start 문제를 해결하기 위한 Warmup 엔드포인트
 * Redis 연결 및 Database 연결을 사전 초기화합니다.
 *
 * Kubernetes Readiness Probe 및 Docker healthcheck로 사용 가능합니다.
 */
@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class WarmupController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JpaUserRepository jpaUserRepository;

    @GetMapping("/warmup")
    public ResponseEntity<Map<String, Object>> warmup() {
        log.info("Starting User Service warmup...");

        Map<String, Object> result = new HashMap<>();
        Map<String, String> components = new HashMap<>();

        try {
            // Redis warmup
            components.put("redis", warmupRedis());

            // Database warmup
            components.put("database", warmupDatabase());

            result.put("status", "success");
            result.put("components", components);
            result.put("timestamp", System.currentTimeMillis());

            log.info("User Service warmup completed successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("User Service warmup failed", e);

            result.put("status", "failure");
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(503).body(result);
        }
    }

    private String warmupRedis() {
        // Simple Redis operation to initialize connection pool
        redisTemplate.opsForValue().get("warmup:user:test");
        log.info("Redis warmup successful");
        return "initialized";
    }

    private String warmupDatabase() {
        // Simple query to initialize connection pool and JPA
        long count = jpaUserRepository.count();
        log.info("Database warmup successful, user count: {}", count);
        return "initialized";
    }
}
