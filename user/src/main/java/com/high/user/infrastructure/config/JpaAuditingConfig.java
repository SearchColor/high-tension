package com.high.user.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // SecurityContext에서 인증 정보 추출
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 인증되지 않은 경우 또는 익명 사용자인 경우 "SYSTEM" 반환
            if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("SYSTEM");
            }

            // 인증된 사용자의 이름(userId) 반환
            // Issue #14(JWT 인증) 구현 후 JWT에서 userId 추출하도록 수정 예정
            String username = authentication.getName();
            return Optional.of(username);
        };
    }
}