package com.high.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration
 * CORS 설정
 */
@Configuration
public class SecurityConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow origins (개발 환경: 모든 origin 허용, 운영 환경에서는 특정 도메인만 허용)
        config.setAllowedOriginPatterns(List.of("*"));

        // Allow methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow headers
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-User-Id",
                "X-User-Role"
        ));

        // Expose headers (클라이언트에서 접근 가능한 헤더)
        config.setExposedHeaders(Arrays.asList(
                "X-User-Id",
                "X-User-Role"
        ));

        // Allow credentials
        config.setAllowCredentials(true);

        // Max age (preflight 요청 캐시 시간)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}