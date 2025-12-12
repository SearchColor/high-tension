package com.high.product.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.library.security.filter.HeaderAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				// 공개 엔드포인트 (필요 시 추가)
				.requestMatchers(
						"/api/v1/health",
						"/v3/api-docs/**",
						"/swagger-ui/**",
						"/swagger-ui.html").permitAll()
				.anyRequest().authenticated()
			);

		// Gateway 헤더 인증 필터
		http.addFilterBefore(
			new HeaderAuthenticationFilter(),
			UsernamePasswordAuthenticationFilter.class
		);

		return http.build();
	}
}


