package com.high.order.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            try {
                ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes == null) {
                    return Optional.empty();
                }

                HttpServletRequest request = attributes.getRequest();
                String userId = request.getHeader("X-User-Id");

                if (userId == null || userId.isBlank()) {
                    return Optional.empty();
                }

                try {
                    return Optional.of(UUID.fromString(userId));
                } catch (IllegalArgumentException e) {
                    return Optional.empty();
                }

            } catch (Exception ex) {
                return Optional.empty();
            }
        };
    }

}