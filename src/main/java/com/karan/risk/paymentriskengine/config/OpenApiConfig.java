package com.karan.risk.paymentriskengine.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI paymentRiskEngineOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Payment Risk Engine API")
                .description("""
                                Real-time fraud detection and risk scoring system for payment transactions.

                                **Core features:**
                                - Payment intake with validation and persistence
                                - Pluggable rule engine (velocity, amount, geo, blacklist)
                                - Risk scoring and decision engine (APPROVE/REVIEW/DECLINE)
                                - Full audit trail with rule hit persistence
                                - Admin API for flagged transactions and manual overrides
                                - JWT-based authentication with role-based access control
                                - Redis-backed distributed velocity counters
                                - Kafka event publishing for downstream services
                                """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Karan Rastogi")
                    .url("https://github.com/Karan-Rastogi"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development")))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("""
                                                Provide the JWT token obtained from /api/v1/auth/login.
                                                Format: Bearer <token>
                                                """)));
    }
}
