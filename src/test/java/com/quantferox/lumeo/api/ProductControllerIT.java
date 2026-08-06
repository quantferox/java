package com.quantferox.lumeo.api;

import com.quantferox.lumeo.BaseIntegrationTest;
import com.quantferox.lumeo.dto.request.LoginRequest;
import com.quantferox.lumeo.dto.request.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Product API - Integration Tests")
class ProductControllerIT extends BaseIntegrationTest {

    private String adminToken;
    private String userToken;

    @BeforeEach
    void obtainTokens() throws Exception {
        adminToken = login("admin",  "admin123");
        userToken  = login("alice",  "user123");
    }

    // ── Public endpoints ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/products - returns paginated list, no auth required")
    void listProducts_noAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/products/featured - returns list without auth")
    void featuredProducts_noAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/search?q=iphone - returns matching results")
    void searchProducts_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/products/search").param("q", "iphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", containsStringIgnoringCase("iphone")));
    }

    // ── Admin-only endpoints ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/products - admin creates product, returns 201")
    void createProduct_asAdmin_returns201() throws Exception {
        ProductRequest req = buildProductRequest();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(org.hamcrest.Matchers.startsWith("Test Product")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.price").value(49.99));
    }

    @Test
    @DisplayName("POST /api/v1/products - regular user gets 403")
    void createProduct_asUser_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProductRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/products - unauthenticated gets 403")
    void createProduct_noAuth_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProductRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/products - missing name returns 400 with fieldErrors")
    void createProduct_missingName_returns400() throws Exception {
        ProductRequest req = buildProductRequest();
        req.setName(null);

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - soft-deletes product, returns 204")
    void deleteProduct_asAdmin_returns204() throws Exception {
        // First create
        ProductRequest req = buildProductRequest();
        String body = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(delete("/api/v1/products/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/products/low-stock - requires ADMIN")
    void lowStock_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/products/low-stock")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private ProductRequest buildProductRequest() {
        long nano = System.nanoTime();
        ProductRequest req = new ProductRequest();
        req.setName("Test Product " + nano);
        req.setSlug("test-product-" + nano);
        req.setSku("TEST-SKU-" + nano);
        req.setDescription("A test product description");
        req.setPrice(new BigDecimal("49.99"));
        req.setStockQuantity(10);
        req.setActive(true);
        req.setCategoryId(1L);  // seeded category
        return req;
    }
}
