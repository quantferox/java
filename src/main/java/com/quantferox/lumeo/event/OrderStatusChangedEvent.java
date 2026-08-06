package com.quantferox.lumeo.event;

import com.quantferox.lumeo.domain.enums.OrderStatus;
import com.quantferox.lumeo.dto.response.OrderResponse;
import org.springframework.context.ApplicationEvent;

/**
 * Published when an order transitions to a new status.
 * Useful for sending shipment notifications, triggering refund workflows, etc.
 */
public class OrderStatusChangedEvent extends ApplicationEvent {

    private final OrderResponse order;
    private final OrderStatus   previousStatus;
    private final OrderStatus   newStatus;

    public OrderStatusChangedEvent(Object source, OrderResponse order,
                                   OrderStatus previousStatus, OrderStatus newStatus) {
        super(source);
        this.order          = order;
        this.previousStatus = previousStatus;
        this.newStatus      = newStatus;
    }

    public OrderResponse getOrder()           { return order; }
    public OrderStatus   getPreviousStatus()  { return previousStatus; }
    public OrderStatus   getNewStatus()       { return newStatus; }
}
