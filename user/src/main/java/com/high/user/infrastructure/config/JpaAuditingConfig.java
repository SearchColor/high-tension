package com.high.user.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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

            // 인증되지 않은 경우 또는 익명 사용자인 경우 "anonymous" 반환
            if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
                return Optional.of("anonymous");
            }

            // 인증된 사용자의 userId 반환 (JWT에서 추출된 값)
            String userId = authentication.getName();
            return Optional.of(userId);
        };
    }
}