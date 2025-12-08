package com.high.gateway.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Rate Limiting Configuration
 *
 * Token Bucket 알고리즘의 파라미터를 관리합니다.
 */
@Getter
@Configuration
public class RateLimitConfig {

    /**
     * 버킷 최대 용량 (tokens)
     * 사용자가 한 번에 사용할 수 있는 최대 토큰 수
     */
    @Value("${rate-limit.max-tokens:100}")
    private int maxTokens;

    /**
     * 토큰 리필 속도 (tokens/sec)
     * 초당 버킷에 추가되는 토큰 수
     */
    @Value("${rate-limit.refill-rate:10}")
    private int refillRate;
}