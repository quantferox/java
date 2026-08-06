package com.quantferox.lumeo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Immutable audit record - written once, never updated.
 * Tracks who did what, on which entity, from which IP.
 *
 * Compliance requirement in banking/gov: every state-changing operation
 * must be traceable with actor + timestamp.
 */
@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_entity",    columnList = "entity_type, entity_id"),
                @Index(name = "idx_audit_actor",     columnList = "actor"),
                @Index(name = "idx_audit_created_at",columnList = "created_at")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username of the principal who triggered the action, or "SYSTEM" for scheduled jobs. */
    @Column(nullable = false, length = 100)
    private String actor;

    /** Action performed - e.g. ORDER_PLACED, PRODUCT_UPDATED, USER_DISABLED. */
    @Column(nullable = false, length = 80)
    private String action;

    /** Domain entity class name - e.g. "Order", "Product". */
    @Column(name = "entity_type", nullable = false, length = 80)
    private String entityType;

    /** PK of the affected entity - nullable for bulk operations. */
    @Column(name = "entity_id")
    private Long entityId;

    /** JSON snapshot of relevant fields before/after change - stored as large text. */
    @Lob
    @Column(name = "payload")
    private String payload;

    /** Client IP address extracted from the HTTP request. */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
