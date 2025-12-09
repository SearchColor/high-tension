package com.high.order.infrastructure.config;

import com.library.security.util.SecurityContextUtil;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            String userIdString = SecurityContextUtil.getCurrentUserIdAsString();
            if (userIdString == null || userIdString.isBlank()) {
                return Optional.empty();
            }
            try {
                return Optional.of(UUID.fromString(userIdString));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        };
    }
}