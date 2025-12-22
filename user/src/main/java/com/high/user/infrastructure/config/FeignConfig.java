package com.high.user.infrastructure.config;

import com.library.security.util.SecurityContextUtil;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign Client 타임아웃 설정
     * 타임아웃 체인: Gateway CB(5s) >= Feign Read(5s) > TimeLimiter(3s)
     * 고아 프로세스 방지를 위해 내부 계층이 외부 계층보다 먼저 타임아웃되도록 설정
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            3, TimeUnit.SECONDS,  // connectTimeout
            5, TimeUnit.SECONDS,  // readTimeout
            true                  // followRedirects
        );
    }

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