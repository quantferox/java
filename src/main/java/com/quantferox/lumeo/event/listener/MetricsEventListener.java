package com.quantferox.lumeo.event.listener;

import com.quantferox.lumeo.domain.enums.OrderStatus;
import com.quantferox.lumeo.event.OrderPlacedEvent;
import com.quantferox.lumeo.event.OrderStatusChangedEvent;
import com.quantferox.lumeo.metrics.LumeoMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Updates Micrometer counters on order domain events.
 * Synchronous (no @Async) - counter increments are cheap and thread-safe.
 */
@Component
@RequiredArgsConstructor
public class MetricsEventListener {

    private final LumeoMetrics metrics;

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        metrics.incrementOrdersPlaced();
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.getNewStatus() == OrderStatus.CANCELLED) {
            metrics.incrementOrdersCancelled();
        } else if (event.getNewStatus() == OrderStatus.DELIVERED) {
            metrics.incrementOrdersDelivered();
        }
    }
}
