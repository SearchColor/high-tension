package com.high.gateway.filter;

import com.high.gateway.config.RateLimitConfig;
import com.high.gateway.exception.RateLimitExceededException;
import com.high.gateway.service.TokenBucketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Rate Limiting Global Filter
 *
 * Token Bucket 알고리즘을 사용하여 Rate Limiting을 적용합니다.
 * 인증된 사용자는 User ID 기반, 비인증 사용자는 IP 기반으로 제한합니다.
 * 서버 부하 방지를 위해 모든 요청에 적용됩니다.
 */
@Slf4j
@Component
@Order(2)  // JWT Filter (HIGHEST_PRECEDENCE) 다음에 실행
@RequiredArgsConstructor
public class RateLimitingGlobalFilter implements GlobalFilter {

    private final TokenBucketService tokenBucketService;
    private final RateLimitConfig rateLimitConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Rate Limiting 키 결정: 인증된 사용자는 User ID, 비인증 사용자는 IP
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String rateLimitKey;

        if (userId != null) {
            // 인증된 사용자: User ID 기반
            rateLimitKey = userId;
        } else {
            // 비인증 사용자: IP 기반
            String clientIp = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            rateLimitKey = "ip:" + clientIp;
        }

        // 토큰 1개 소비 시도
        return tokenBucketService.tryConsume(rateLimitKey, 1)
                .flatMap(allowed -> {
                    if (!allowed) {
                        // Rate Limit 초과 - Retry-After 시간 계산
                        int retryAfter = (int) Math.ceil(1.0 / rateLimitConfig.getRefillRate());
                        log.warn("Rate limit exceeded for key: {}, retry after: {}s", rateLimitKey, retryAfter);
                        throw new RateLimitExceededException(retryAfter);
                    }

                    // Rate Limit 헤더 추가 (Response 헤더에)
                    return tokenBucketService.getRemainingTokens(rateLimitKey)
                            .flatMap(remaining -> {
                                // Response 헤더에 Rate Limit 정보 추가
                                exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(rateLimitConfig.getMaxTokens()));
                                exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));

                                log.debug("Rate limit check passed for key: {}, remaining: {}/{}",
                                        rateLimitKey, remaining, rateLimitConfig.getMaxTokens());

                                return chain.filter(exchange);
                            });
                });
    }
}