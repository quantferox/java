package com.quantferox.lumeo.security;

import com.quantferox.lumeo.domain.entity.Order;
import com.quantferox.lumeo.domain.entity.User;
import com.quantferox.lumeo.domain.enums.OrderStatus;
import com.quantferox.lumeo.domain.enums.Role;
import com.quantferox.lumeo.repository.OrderRepository;
import com.quantferox.lumeo.security.permission.LumeoPermissionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LumeoPermissionEvaluator.
 * Uses Mockito - no Spring context needed, runs fast.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LumeoPermissionEvaluator - Unit Tests")
class LumeoPermissionEvaluatorTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    LumeoPermissionEvaluator evaluator;

    private Order pendingOrder;
    private Order confirmedOrder;

    @BeforeEach
    void setup() {
        User alice = User.builder()
                .username("alice").email("alice@test.com")
                .password("x").role(Role.ROLE_USER).enabled(true)
                .build();

        pendingOrder = Order.builder()
                .orderNumber("ORD-001").status(OrderStatus.PENDING)
                .user(alice).totalAmount(BigDecimal.TEN)
                .build();

        confirmedOrder = Order.builder()
                .orderNumber("ORD-002").status(OrderStatus.CONFIRMED)
                .user(alice).totalAmount(BigDecimal.TEN)
                .build();
    }

    // ── ADMIN tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("ADMIN can READ any order")
    void admin_canRead_anyOrder() {
        // ADMIN check short-circuits before hitting the repository
        assertThat(evaluator.hasPermission(adminAuth(), 1L, "Order", "READ")).isTrue();
    }

    @Test
    @DisplayName("ADMIN can WRITE any order regardless of status")
    void admin_canWrite_anyOrder() {
        // ADMIN check short-circuits before hitting the repository
        assertThat(evaluator.hasPermission(adminAuth(), 2L, "Order", "WRITE")).isTrue();
    }

    // ── Owner READ tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("Owner can READ their own order")
    void owner_canRead_ownOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        assertThat(evaluator.hasPermission(userAuth("alice"), 1L, "Order", "READ")).isTrue();
    }

    @Test
    @DisplayName("Non-owner cannot READ someone else's order")
    void nonOwner_cannotRead_othersOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        assertThat(evaluator.hasPermission(userAuth("bob"), 1L, "Order", "READ")).isFalse();
    }

    // ── Owner WRITE (cancel) tests ────────────────────────────────────────

    @Test
    @DisplayName("Owner can WRITE (cancel) their own PENDING order")
    void owner_canWrite_ownPendingOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        assertThat(evaluator.hasPermission(userAuth("alice"), 1L, "Order", "WRITE")).isTrue();
    }

    @Test
    @DisplayName("Owner cannot WRITE (cancel) their CONFIRMED order")
    void owner_cannotWrite_confirmedOrder() {
        when(orderRepository.findById(2L)).thenReturn(Optional.of(confirmedOrder));
        assertThat(evaluator.hasPermission(userAuth("alice"), 2L, "Order", "WRITE")).isFalse();
    }

    @Test
    @DisplayName("Non-owner cannot WRITE someone else's order")
    void nonOwner_cannotWrite_othersOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        assertThat(evaluator.hasPermission(userAuth("bob"), 1L, "Order", "WRITE")).isFalse();
    }

    @Test
    @DisplayName("Returns false for unknown targetType")
    void unknownTargetType_returnsFalse() {
        assertThat(evaluator.hasPermission(userAuth("alice"), 1L, "Invoice", "READ")).isFalse();
    }

    @Test
    @DisplayName("Returns false for non-existent order ID")
    void nonExistentOrder_returnsFalse() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(evaluator.hasPermission(userAuth("alice"), 99L, "Order", "READ")).isFalse();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                "admin", "x",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private Authentication userAuth(String username) {
        return new UsernamePasswordAuthenticationToken(
                username, "x",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
