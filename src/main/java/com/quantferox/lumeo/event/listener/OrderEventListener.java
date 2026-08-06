package com.quantferox.lumeo.event.listener;

import com.quantferox.lumeo.event.OrderPlacedEvent;
import com.quantferox.lumeo.event.OrderStatusChangedEvent;
import com.quantferox.lumeo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens to order domain events and delegates to {@link NotificationService}.
 *
 * {@code @Async} means every handler runs in the async thread pool -
 * the HTTP request returns immediately and email/audit happen in background.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("[EVENT] OrderPlaced - order={} user={}",
                event.getOrder().getOrderNumber(),
                event.getOrder().getUserFullName());
        notificationService.sendOrderConfirmation(event.getOrder());
    }

    @Async
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("[EVENT] OrderStatusChanged - order={} {} → {}",
                event.getOrder().getOrderNumber(),
                event.getPreviousStatus(),
                event.getNewStatus());
        notificationService.sendStatusUpdateNotification(event.getOrder(), event.getNewStatus());
    }
}
