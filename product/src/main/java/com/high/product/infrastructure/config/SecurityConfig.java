package com.high.product.infrastructure.config;

// import com.high.product.infrastructure.security.HeaderAuthenticationFilter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
// @EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
		DefaultAuthenticationEventPublisher publisher)
		throws Exception {

		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			// .addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

			// .authorizeHttpRequests(auth -> auth
			// 	.requestMatchers(
			// 		"/api/v1/**"
			// 	).permitAll()
			//
			// 	.anyRequest().authenticated()
			// );
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll() // 모든 요청 허용
			);

		return http.build();
	}
}

