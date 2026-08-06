package com.quantferox.lumeo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lumeoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lumeo E-Commerce API")
                        .description("""
                                Senior-level Spring Boot REST API demo.
                                
                                **Auth:** POST `/api/v1/auth/login` → copy the `token` value →
                                click **Authorize** → paste as `Bearer <token>`.
                                
                                **Default users:**
                                - `admin / admin123` (ROLE_ADMIN)
                                - `alice / user123`  (ROLE_USER)
                                - `bob   / user123`  (ROLE_USER)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("QuantFerox")
                                .url("https://github.com/quantferox"))
                        .license(new License().name("MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local dev")));
    }
}
