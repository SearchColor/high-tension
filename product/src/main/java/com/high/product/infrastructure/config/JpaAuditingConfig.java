package com.high.product.infrastructure.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.high.product.infrastructure.context.MessageContext;
import com.library.security.util.SecurityContextUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

	@Bean
	public AuditorAware<UUID> auditorProvider() {
		return () -> {
			// 1. Kafka 메시지 기반 (ThreadLocal)
			UUID messageUserId = MessageContext.getUserId();
			if (messageUserId != null) {
				return Optional.of(messageUserId);
			}

			// 2. HTTP 기반(SecurityContext)
			String userIdString = SecurityContextUtil.getCurrentUserIdAsString();
			if (userIdString != null && !userIdString.isBlank()) {
				try {
					return Optional.of(UUID.fromString(userIdString));
				} catch (IllegalArgumentException ignored) {}
			}

			return Optional.empty();
		};
	}

}