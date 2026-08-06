package com.quantferox.lumeo.domain.enums;

/**
 * Lifecycle states of an {@link com.quantferox.lumeo.domain.entity.Order}.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
