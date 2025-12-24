package com.high.external.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearer-jwt";
        return new OpenAPI()
                .info(new Info()
                        .title("High-Tension User Service API")
                        .version("v1.0.0")
                        .description("External Service API 문서입니다. JWT 인증이 필요한 API는 Authorize 버튼을 클릭하여 토큰을 입력하세요.")
                        .contact(new Contact()
                                .name("박용재")
                                .email("gypark@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8000").description("Gateway 경유")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")));
    }
}
