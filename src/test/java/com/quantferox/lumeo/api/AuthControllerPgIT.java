package com.quantferox.lumeo.api;

import com.quantferox.lumeo.PostgresIntegrationTest;
import com.quantferox.lumeo.dto.request.LoginRequest;
import com.quantferox.lumeo.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Auth tests running against real PostgreSQL via Testcontainers.
 * Verifies that Liquibase schema and JPA behavior match production.
 *
 * Skip in CI without Docker: mvn test -Dexcludes="**&#47;*PgIT.java"
 */
@DisplayName("Auth API - PostgreSQL Integration Tests")
class AuthControllerPgIT extends PostgresIntegrationTest {

    @Test
    @DisplayName("register + login round-trip works on real Postgres")
    void registerAndLogin_roundTrip() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("pguser");
        reg.setEmail("pguser@test.com");
        reg.setPassword("password123");
        reg.setFirstName("PG");
        reg.setLastName("User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("pguser"));

        LoginRequest login = new LoginRequest();
        login.setUsername("pguser");
        login.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("login with seeded admin works on real Postgres")
    void login_seededAdmin_works() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ROLE_ADMIN"));
    }
}
