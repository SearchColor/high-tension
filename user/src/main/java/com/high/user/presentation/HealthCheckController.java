package com.high.user.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 초기 세팅 확인용 Health Check Controller
 * Issue #12 완료 후 삭제 예정
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.security.jwt.access-token-validity}")
    private Long accessTokenValidity;

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

    // URL에서 비밀번호 마스킹
    private String maskPassword(String url) {
        return url.replaceAll("password=[^&;]*", "password=****");
    }
}