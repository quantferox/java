package com.quantferox.lumeo.event.listener;

import com.quantferox.lumeo.event.OrderPlacedEvent;
import com.quantferox.lumeo.event.OrderStatusChangedEvent;
import com.quantferox.lumeo.event.LowStockEvent;
import com.quantferox.lumeo.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens to all domain events and writes audit log entries.
 * Completely decoupled from business logic - add new events here without
 * touching the service layer.
 */
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogService auditLogService;

    @Async
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        auditLogService.logSystem(
                "ORDER_PLACED",
                "Order",
                event.getOrder().getId(),
                event.getOrder());
    }

    @Async
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        auditLogService.logSystem(
                "ORDER_STATUS_CHANGED_TO_" + event.getNewStatus(),
                "Order",
                event.getOrder().getId(),
                event.getOrder());
    }

    @Async
    @EventListener
    public void onLowStock(LowStockEvent event) {
        auditLogService.logSystem(
                "LOW_STOCK_ALERT",
                "Product",
                event.getProduct().getId(),
                event.getProduct());
    }
}
