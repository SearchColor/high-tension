package com.high.product.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.addSecurityItem(
				new SecurityRequirement().addList("bearerAuth")
			)
			.servers(List.of(
				new Server()
					.url("http://localhost:8200")
					.description("Product Service (Local)"),
				new Server()
					.url("http://localhost:8000")
					.description("API Gateway 경유")
			))
			.components(
				new Components()
					.addSecuritySchemes(
						"bearerAuth",
						new SecurityScheme()
							.type(SecurityScheme.Type.HTTP)
							.scheme("bearer")
							.bearerFormat("JWT")
							.in(SecurityScheme.In.HEADER)
							.name("Authorization")
					)
			);
	}
}
