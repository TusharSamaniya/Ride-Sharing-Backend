package com.rideshare.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideShare Backend API")
                        .description("Complete REST API for "
                                + "RideShare application. "
                                + "Built with Spring Boot 3, "
                                + "JWT, Redis, Kafka, WebSocket.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your@email.com")))

                // This adds the Authorize button in Swagger UI
                // so you can test protected endpoints
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes(
                                "Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your "
                                                + "JWT token here")));
    }
}