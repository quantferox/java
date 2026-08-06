package com.quantferox.lumeo.security.permission;

import com.quantferox.lumeo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom {@link PermissionEvaluator} for domain-object-level security.
 *
 * Enables fine-grained {@code @PreAuthorize} expressions like:
 * <pre>
 *   @PreAuthorize("hasPermission(#orderId, 'Order', 'READ')")
 *   @PreAuthorize("hasPermission(#orderId, 'Order', 'WRITE')")
 * </pre>
 *
 * Rules:
 * <ul>
 *   <li>ADMIN can do anything on any entity.</li>
 *   <li>USER can READ their own orders.</li>
 *   <li>USER can WRITE (cancel) their own PENDING orders only.</li>
 * </ul>
 *
 * This replaces scattered {@code isOwner} checks in service methods with
 * a single, testable, declarative security policy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LumeoPermissionEvaluator implements PermissionEvaluator {

    private final OrderRepository orderRepository;

    // ── hasPermission(authentication, targetDomainObject, permission) ────
    // Used when the object itself is passed: @PreAuthorize("hasPermission(#order, 'READ')")

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject,
                                 Object permission) {
        if (auth == null || !auth.isAuthenticated()) return false;
        if (isAdmin(auth)) return true;

        log.debug("[PERMISSION] actor={} target={} permission={}",
                auth.getName(), targetDomainObject, permission);
        // Extend here for other domain objects as the project grows
        return false;
    }

    // ── hasPermission(authentication, targetId, targetType, permission) ──
    // Used with ID + type string: @PreAuthorize("hasPermission(#orderId, 'Order', 'READ')")

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId,
                                 String targetType, Object permission) {
        if (auth == null || !auth.isAuthenticated()) return false;
        if (isAdmin(auth)) return true;

        String actor = auth.getName();
        String perm  = permission.toString().toUpperCase();

        log.debug("[PERMISSION] actor={} targetType={} targetId={} permission={}",
                actor, targetType, targetId, perm);

        return switch (targetType) {
            case "Order" -> evaluateOrderPermission(actor, (Long) targetId, perm);
            default -> {
                log.warn("[PERMISSION] Unknown targetType '{}' - denying", targetType);
                yield false;
            }
        };
    }

    // ── Domain-specific evaluators ────────────────────────────────────────

    private boolean evaluateOrderPermission(String username, Long orderId, String permission) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    boolean isOwner = order.getUser().getUsername().equals(username);
                    return switch (permission) {
                        // Any user can read their own order
                        case "READ"  -> isOwner;
                        // User can only WRITE (cancel) their own PENDING order
                        case "WRITE" -> isOwner && order.getStatus().name().equals("PENDING");
                        default -> false;
                    };
                })
                .orElse(false);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
