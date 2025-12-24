package com.high.payment.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String BEARER_AUTH = "bearerAuth";

	@Bean
	public OpenAPI paymentOpenAPI() {
		return new OpenAPI()
			.info(new Info()
					  .title("High-Tension Payment Service API")
					  .description("payment-service 결제 생성/조회/취소 및 Iamport Webhook 처리")
					  .version("v1"))
			.components(new Components()
							.addSecuritySchemes(BEARER_AUTH,
												new SecurityScheme()
													.name(BEARER_AUTH)
													.type(SecurityScheme.Type.HTTP)
													.scheme("bearer")
													.bearerFormat("JWT")
							)
			)
			.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
	}
}
