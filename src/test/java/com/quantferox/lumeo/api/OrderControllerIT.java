package com.quantferox.lumeo.api;

import com.quantferox.lumeo.BaseIntegrationTest;
import com.quantferox.lumeo.dto.request.LoginRequest;
import com.quantferox.lumeo.dto.request.OrderRequest;
import com.quantferox.lumeo.dto.request.OrderStatusRequest;
import com.quantferox.lumeo.domain.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Order API - Integration Tests")
class OrderControllerIT extends BaseIntegrationTest {

    private String adminToken;
    private String aliceToken;

    @BeforeEach
    void obtainTokens() throws Exception {
        adminToken = login("admin", "admin123");
        aliceToken = login("alice", "user123");
    }

    // ── Place order ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/orders - authenticated user places order, returns 201")
    void placeOrder_success_returns201() throws Exception {
        OrderRequest req = buildOrderRequest(1L, 1);  // product id=1, qty=1

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalAmount").isNumber());
    }

    @Test
    @DisplayName("POST /api/v1/orders - unauthenticated returns 403")
    void placeOrder_noAuth_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOrderRequest(1L, 1))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/orders - empty items list returns 400")
    void placeOrder_emptyItems_returns400() throws Exception {
        OrderRequest req = new OrderRequest();
        req.setItems(List.of());
        req.setShippingStreet("123 Main St");
        req.setShippingCity("NY");
        req.setShippingZip("10001");
        req.setShippingCountry("USA");

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.items").exists());
    }

    // ── Admin: list all orders ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/orders - admin gets all orders paginated")
    void listAllOrders_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/orders - regular user gets 403")
    void listAllOrders_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isForbidden());
    }

    // ── Status update ──────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /api/v1/orders/{id}/status - admin transitions PENDING→CONFIRMED")
    void updateStatus_adminConfirms_returns200() throws Exception {
        // Place order first
        String body = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOrderRequest(1L, 1))))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(body).get("id").asLong();

        // Admin confirms it
        OrderStatusRequest statusReq = new OrderStatusRequest();
        statusReq.setStatus(OrderStatus.CONFIRMED);

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{id}/status - invalid transition returns 422")
    void updateStatus_invalidTransition_returns422() throws Exception {
        // Place order
        String body = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOrderRequest(1L, 1))))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(body).get("id").asLong();

        // Try illegal PENDING → DELIVERED (must go PENDING→CONFIRMED→SHIPPED→DELIVERED)
        OrderStatusRequest statusReq = new OrderStatusRequest();
        statusReq.setStatus(OrderStatus.DELIVERED);

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Invalid order status transition")));
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

    private OrderRequest buildOrderRequest(Long productId, int quantity) {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);

        OrderRequest req = new OrderRequest();
        req.setItems(List.of(item));
        req.setShippingStreet("123 Main St");
        req.setShippingCity("Test City");
        req.setShippingState("TX");
        req.setShippingZip("75001");
        req.setShippingCountry("USA");
        return req;
    }
}
