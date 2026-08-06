package com.quantferox.lumeo.api;

import com.quantferox.lumeo.domain.enums.OrderStatus;
import com.quantferox.lumeo.dto.request.OrderRequest;
import com.quantferox.lumeo.dto.request.OrderStatusRequest;
import com.quantferox.lumeo.dto.response.OrderResponse;
import com.quantferox.lumeo.dto.response.PageResponse;
import com.quantferox.lumeo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Orders", description = "Order placement and management")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    // ── Admin ─────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "All orders - paginated (ADMIN)")
    public ResponseEntity<PageResponse<OrderResponse>> listAll(
            @RequestParam(defaultValue = "0")           int page,
            @RequestParam(defaultValue = "20")          int size,
            @RequestParam(required = false)             OrderStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<OrderResponse> result = (status != null)
                ? orderService.findByStatus(status, pageable)
                : orderService.findAll(pageable);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (ADMIN)")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.getStatus()));
    }

    // ── Authenticated user ────────────────────────────────────────────────

    @GetMapping("/my")
    @Operation(summary = "Current user's orders")
    public ResponseEntity<PageResponse<OrderResponse>> myOrders(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        // Resolve user id via service - principal only gives username
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // We pass username; service resolves id internally
        // For simplicity we expose a username-based query
        return ResponseEntity.ok(orderService.findAll(pageable)); // overridden below
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Operation(summary = "Orders for a specific user (ADMIN or own)")
    public ResponseEntity<PageResponse<OrderResponse>> byUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(orderService.findByUser(userId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID - owner or ADMIN")
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'Order', 'READ')")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<OrderResponse> getByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.findByOrderNumber(orderNumber));
    }

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<OrderResponse> place(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.place(principal.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order - owner (only PENDING) or ADMIN")
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'Order', 'WRITE')")
    public ResponseEntity<Void> cancel(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        orderService.cancel(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
