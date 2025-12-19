package com.high.gateway.filter;

import com.high.gateway.exception.GatewayErrorCode;
import com.high.gateway.exception.UnauthorizedException;
import com.high.gateway.service.JwtTokenValidator;
import com.high.gateway.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Global Filter
 * 모든 요청에 대한 JWT 검증 수행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private final JwtTokenValidator jwtTokenValidator;
    private final TokenBlacklistService tokenBlacklistService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 인증 제외 경로
    private static final List<String> EXCLUDED_PATHS = List.of(
            // User Service - Public API
            "/api/v1/users/signup",
            "/api/v1/users/login",
            "/api/v1/users/reissue",

            // Passkey Authentication (Public - No JWT required)
            "/api/v1/passkeys/authenticate/start",
            "/api/v1/passkeys/authenticate/finish",
            "/passkey-test.html",

            // Swagger UI & OpenAPI Docs
            "/docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/*/v3/api-docs/**",  // /{service}/v3/api-docs/**
            "/webjars/**",

            // Monitoring
            "/actuator/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 제외 경로 확인
        if (isExcludedPath(path)) {
            log.debug("Excluded path accessed: {}", path);
            return chain.filter(exchange);
        }

        // Authorization 헤더에서 토큰 추출
        String token = extractToken(exchange.getRequest());
        if (token == null) {
            log.warn("Token not found in request: {}", path);
            return Mono.error(new UnauthorizedException(GatewayErrorCode.TOKEN_NOT_FOUND));
        }

        // JWT 검증
        if (!jwtTokenValidator.validateToken(token)) {
            log.warn("Invalid token for path: {}", path);
            return Mono.error(new UnauthorizedException(GatewayErrorCode.INVALID_TOKEN));
        }

        // 블랙리스트 확인 (비동기)
        return tokenBlacklistService.isBlacklisted(token)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        log.warn("Blacklisted token used for path: {}", path);
                        return Mono.error(new UnauthorizedException(GatewayErrorCode.BLACKLISTED_TOKEN));
                    }

                    // 사용자 정보 추출 및 헤더 추가
                    String userId = jwtTokenValidator.getUserId(token);
                    String role = jwtTokenValidator.getRole(token);

                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Role", role)
                            .build();

                    log.debug("Authenticated request - Path: {}, UserId: {}, Role: {}", path, userId, role);

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                });
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 제외 경로 확인
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
