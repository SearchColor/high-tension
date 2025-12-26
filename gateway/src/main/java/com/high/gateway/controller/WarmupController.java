package com.high.gateway.controller;

import com.high.gateway.service.TokenBucketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway Warmup Controller
 *
 * 서비스 재시작 후 Cold Start 문제를 해결하기 위한 Warmup 엔드포인트
 * Redis 연결 및 TokenBucket 서비스를 사전 초기화합니다.
 *
 * Kubernetes Readiness Probe 및 Docker healthcheck로 사용 가능합니다.
 */
@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class WarmupController {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final TokenBucketService tokenBucketService;

    @GetMapping("/warmup")
    public Mono<ResponseEntity<Map<String, Object>>> warmup() {
        log.info("Starting Gateway warmup...");

        return Mono.zip(
            warmupRedis(),
            warmupTokenBucket()
        ).map(tuple -> {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("components", Map.of(
                "redis", tuple.getT1(),
                "tokenBucket", tuple.getT2()
            ));
            result.put("timestamp", System.currentTimeMillis());

            log.info("Gateway warmup completed successfully");
            return ResponseEntity.ok(result);
        }).onErrorResume(error -> {
            log.error("Gateway warmup failed", error);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "failure");
            result.put("error", error.getMessage());
            result.put("timestamp", System.currentTimeMillis());

            return Mono.just(ResponseEntity.status(503).body(result));
        });
    }

    private Mono<String> warmupRedis() {
        return reactiveRedisTemplate.opsForValue()
            .get("warmup:gateway:test")
            .doOnSuccess(v -> log.info("Redis warmup successful"))
            .defaultIfEmpty("ok")
            .map(v -> "initialized");
    }

    private Mono<String> warmupTokenBucket() {
        return tokenBucketService.tryConsume("warmup:gateway:test", 1)
            .doOnSuccess(success -> log.info("TokenBucket warmup successful: {}", success))
            .map(success -> "initialized");
    }
}
