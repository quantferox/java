package com.quantferox.lumeo.event;

import com.quantferox.lumeo.dto.response.OrderResponse;
import org.springframework.context.ApplicationEvent;

/**
 * Published by {@link com.quantferox.lumeo.service.OrderService}
 * immediately after an order is persisted.
 *
 * Listeners handle confirmation email, audit log, metrics - all decoupled.
 */
public class OrderPlacedEvent extends ApplicationEvent {

    private final OrderResponse order;

    public OrderPlacedEvent(Object source, OrderResponse order) {
        super(source);
        this.order = order;
    }

    public OrderResponse getOrder() {
        return order;
    }
}
