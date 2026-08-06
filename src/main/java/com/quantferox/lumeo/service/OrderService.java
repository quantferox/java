package com.quantferox.lumeo.service;

import com.quantferox.lumeo.domain.entity.Order;
import com.quantferox.lumeo.domain.entity.OrderItem;
import com.quantferox.lumeo.domain.entity.Product;
import com.quantferox.lumeo.domain.entity.User;
import com.quantferox.lumeo.domain.enums.OrderStatus;
import com.quantferox.lumeo.dto.request.OrderRequest;
import com.quantferox.lumeo.dto.response.OrderResponse;
import com.quantferox.lumeo.dto.response.PageResponse;
import com.quantferox.lumeo.event.LowStockEvent;
import com.quantferox.lumeo.event.OrderPlacedEvent;
import com.quantferox.lumeo.event.OrderStatusChangedEvent;
import com.quantferox.lumeo.exception.InsufficientStockException;
import com.quantferox.lumeo.exception.OrderStatusTransitionException;
import com.quantferox.lumeo.exception.ResourceNotFoundException;
import com.quantferox.lumeo.mapper.OrderMapper;
import com.quantferox.lumeo.repository.OrderRepository;
import com.quantferox.lumeo.repository.ProductRepository;
import com.quantferox.lumeo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
/*
 * Class-level default: readOnly = true, propagation = REQUIRED.
 * Write methods override with explicit isolation and propagation.
 *
 * Isolation levels used in this service:
 *
 *  READ_COMMITTED (default in Postgres):
 *    Each statement sees only data committed before that statement began.
 *    Good for most reads - prevents dirty reads.
 *
 *  REPEATABLE_READ:
 *    All reads within the transaction see the same snapshot.
 *    Used for order placement to prevent phantom reads on stock quantities
 *    (two concurrent orders for the last item must not both succeed).
 *
 *  SERIALIZABLE:
 *    Strongest guarantee - transactions execute as if run serially.
 *    Used for status transitions to prevent two admins transitioning
 *    the same order simultaneously.
 */
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.PENDING,   EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPED,   OrderStatus.CANCELLED),
            OrderStatus.SHIPPED,   EnumSet.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, EnumSet.of(OrderStatus.REFUNDED),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.REFUNDED,  EnumSet.noneOf(OrderStatus.class)
    );

    private final OrderRepository        orderRepository;
    private final ProductRepository      productRepository;
    private final UserRepository         userRepository;
    private final OrderMapper            orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ── Queries (readOnly = true inherited, READ_COMMITTED is sufficient) ──

    public PageResponse<OrderResponse> findAll(Pageable pageable) {
        return PageResponse.of(orderRepository.findAll(pageable).map(orderMapper::toResponse));
    }

    public PageResponse<OrderResponse> findByUser(Long userId, Pageable pageable) {
        return PageResponse.of(
                orderRepository.findAllByUserId(userId, pageable).map(orderMapper::toResponse));
    }

    public PageResponse<OrderResponse> findByStatus(OrderStatus status, Pageable pageable) {
        return PageResponse.of(
                orderRepository.findAllByStatus(status, pageable).map(orderMapper::toResponse));
    }

    public OrderResponse findById(Long id) {
        return orderMapper.toResponse(getOrThrow(id));
    }

    public OrderResponse findByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return orderMapper.toResponse(order);
    }

    public BigDecimal getTotalRevenue() {
        return orderRepository.sumTotalByStatus(OrderStatus.DELIVERED);
    }

    // ── place() - REPEATABLE_READ ─────────────────────────────────────────
    //
    // Why REPEATABLE_READ here?
    // When we read product.stockQuantity and then decrement it, a concurrent
    // transaction must not be able to read the same (old) stock value and
    // also place an order for the same item.
    // REPEATABLE_READ ensures our stock read is stable for the entire transaction.
    // Combined with optimistic locking (@Version on Product), this gives us
    // the strongest practical guarantee without SERIALIZABLE overhead.

    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation   = Isolation.REPEATABLE_READ
    )
    public OrderResponse place(String username, OrderRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .user(user)
                .shippingStreet(request.getShippingStreet())
                .shippingCity(request.getShippingCity())
                .shippingState(request.getShippingState())
                .shippingZip(request.getShippingZip())
                .shippingCountry(request.getShippingCountry())
                .notes(request.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(), itemReq.getQuantity(), product.getStockQuantity());
            }
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());

            if (product.getStockQuantity() <= LOW_STOCK_THRESHOLD) {
                eventPublisher.publishEvent(new LowStockEvent(this, buildProductSnapshot(product)));
            }

            order.addItem(OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .productName(product.getName())
                    .build());
        }

        order.recalculateTotal();
        Order saved = orderRepository.save(order);
        OrderResponse response = orderMapper.toResponse(saved);

        eventPublisher.publishEvent(new OrderPlacedEvent(this, response));
        log.info("Placed order id={} number={} total={}", saved.getId(),
                saved.getOrderNumber(), saved.getTotalAmount());
        return response;
    }

    // ── updateStatus() - SERIALIZABLE ────────────────────────────────────
    //
    // Why SERIALIZABLE here?
    // Two concurrent admin requests transitioning the same order (e.g. both
    // trying to CONFIRM it) must not both succeed. SERIALIZABLE detects the
    // conflict and rolls back one of them with a serialization failure,
    // which the client retries. The order FSM is then correctly advanced once.

    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation   = Isolation.SERIALIZABLE
    )
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = getOrThrow(id);
        OrderStatus current = order.getStatus();

        Set<OrderStatus> allowed = TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new OrderStatusTransitionException(current, newStatus);
        }
        order.setStatus(newStatus);
        OrderResponse response = orderMapper.toResponse(order);

        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, response, current, newStatus));
        log.info("Order id={} status: {} -> {}", id, current, newStatus);
        return response;
    }

    // ── cancel() - delegates, inherits SERIALIZABLE from updateStatus ────

    @Transactional(propagation = Propagation.REQUIRED)
    public void cancel(Long id, String requestingUsername) {
        Order order = getOrThrow(id);
        boolean isOwner = order.getUser().getUsername().equals(requestingUsername);
        boolean isAdmin = userRepository.findByUsername(requestingUsername)
                .map(u -> u.getRole().name().equals("ROLE_ADMIN"))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Not authorized to cancel this order");
        }
        updateStatus(id, OrderStatus.CANCELLED);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Order getOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private String generateOrderNumber() {
        return "ORD-" + Instant.now().getEpochSecond() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private com.quantferox.lumeo.dto.response.ProductResponse buildProductSnapshot(Product p) {
        return com.quantferox.lumeo.dto.response.ProductResponse.builder()
                .id(p.getId()).name(p.getName()).sku(p.getSku())
                .slug(p.getSlug()).price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .build();
    }
}
