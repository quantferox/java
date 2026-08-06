package com.quantferox.lumeo.api;

import com.quantferox.lumeo.BaseIntegrationTest;
import com.quantferox.lumeo.dto.request.LoginRequest;
import com.quantferox.lumeo.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Auth API - Integration Tests")
class AuthControllerIT extends BaseIntegrationTest {

    // ── Register ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/auth/register - success returns 201 + user body")
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("newuser@test.com");
        req.setPassword("password123");
        req.setFirstName("New");
        req.setLastName("User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - duplicate username returns 409")
    void register_duplicateUsername_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("admin");  // seeded user
        req.setEmail("unique@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - blank username returns 400 with field errors")
    void register_blankUsername_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("");
        req.setEmail("test@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists());
    }

    // ── Login ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/auth/login - valid credentials return JWT token")
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("admin"))
                .andExpect(jsonPath("$.user.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - blank fields return 400")
    void login_blankFields_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("");
        req.setPassword("");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}
