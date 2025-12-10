package com.high.product.infrastructure.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.library.security.util.SecurityContextUtil;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

	@Bean
	public AuditorAware<UUID> auditorProvider() {
		return SecurityContextUtil::getCurrentUserIdForAuditing;
	}
}