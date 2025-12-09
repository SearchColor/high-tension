package com.high.product.infrastructure.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.library.security.util.SecurityContextUtil;

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