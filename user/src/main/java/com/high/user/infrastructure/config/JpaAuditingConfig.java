package com.high.user.infrastructure.config;

import com.library.security.util.SecurityContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    // System user UUID for anonymous operations (signup, etc.)
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            Optional<UUID> userId = SecurityContextUtil.getCurrentUserIdForAuditing();
            // Return system UUID if no authenticated user (e.g., during signup)
            return Optional.of(userId.orElse(SYSTEM_USER_ID));
        };
    }
}