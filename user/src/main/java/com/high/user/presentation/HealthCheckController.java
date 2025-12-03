package com.high.user.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 초기 세팅 확인용 Health Check Controller
 * Issue #14 완료 후 삭제 예정
 */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.security.jwt.access-token-validity}")
    private Long accessTokenValidity;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", applicationName);
        response.put("timestamp", LocalDateTime.now());
        response.put("datasource", maskPassword(datasourceUrl));
        response.put("jwtAccessTokenValidity", accessTokenValidity + "ms");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> dbCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "DB connection check");
        response.put("datasource", maskPassword(datasourceUrl));
        response.put("message", "If you see this, Spring Boot started successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/security")
    public ResponseEntity<Map<String, Object>> securityCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Security configuration check");
        response.put("message", "Security filter chain is active");
        response.put("jwtValidity", accessTokenValidity + "ms");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> redisCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Redis 연결 테스트: PING 명령어 실행
            String pingResult = redisTemplate.getConnectionFactory()
                .getConnection()
                .ping();

            // 테스트 데이터 저장 및 조회
            String testKey = "health_check_test";
            String testValue = "Redis is working!";
            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            response.put("status", "UP");
            response.put("redis", redisHost + ":" + redisPort);
            response.put("ping", pingResult);
            response.put("test", "Set and Get operation successful");
            response.put("testValue", retrievedValue);
            response.put("message", "Redis connection is healthy");

        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("redis", redisHost + ":" + redisPort);
            response.put("error", e.getMessage());
            response.put("message", "Redis connection failed. Please check if Redis server is running.");
        }

        return ResponseEntity.ok(response);
    }

    // URL에서 비밀번호 마스킹
    private String maskPassword(String url) {
        return url.replaceAll("password=[^&;]*", "password=****");
    }
}