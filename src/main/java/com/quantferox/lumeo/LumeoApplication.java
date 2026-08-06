package com.quantferox.lumeo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Lumeo - Senior-level Spring Boot e-commerce platform.
 *
 * <p>Key features:
 * <ul>
 *   <li>Full CRUD for Products, Categories, Orders, Users</li>
 *   <li>REST API at {@code /api/v1/**} - JWT-secured, documented with Swagger UI at {@code /swagger-ui.html}</li>
 *   <li>Thymeleaf storefront at {@code /shop} and admin panel at {@code /admin}</li>
 *   <li>Spring Security: stateless JWT for API, form-login session for UI</li>
 *   <li>JPA auditing ({@code createdAt}/{@code updatedAt}), optimistic locking ({@code @Version})</li>
 *   <li>MapStruct mappers, Bean Validation, paginated responses</li>
 *   <li>H2 in-memory DB - swap datasource in {@code application.yaml} for Postgres/MySQL</li>
 *   <li>H2 console at {@code /h2-console}, Actuator at {@code /actuator}</li>
 * </ul>
 *
 * <p>Default seed credentials: {@code admin/admin123} | {@code alice/user123} | {@code bob/user123}
 */
@SpringBootApplication
@EnableJpaAuditing
public class LumeoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LumeoApplication.class, args);
    }
}
