package com.high.user.infrastructure.config;

import com.library.security.util.SecurityContextUtil;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * Feign 요청 시 X-User-Id, X-User-Role 헤더 전달
     * SecurityContext에서 현재 사용자 정보를 가져와 헤더에 추가
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            try {
                String userId = SecurityContextUtil.getCurrentUserIdAsString();
                String role = SecurityContextUtil.getCurrentUserRole();

                if (userId != null) {
                    template.header("X-User-Id", userId);
                    log.debug("Feign request - X-User-Id: {}", userId);
                }

                if (role != null) {
                    template.header("X-User-Role", role);
                    log.debug("Feign request - X-User-Role: {}", role);
                }
            } catch (Exception e) {
                log.warn("Failed to add user headers to Feign request: {}", e.getMessage());
            }
        };
    }
}