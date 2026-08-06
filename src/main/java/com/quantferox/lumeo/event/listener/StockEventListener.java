package com.quantferox.lumeo.event.listener;

import com.quantferox.lumeo.event.LowStockEvent;
import com.quantferox.lumeo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void onLowStock(LowStockEvent event) {
        log.warn("[EVENT] LowStock - product='{}' sku={} qty={}",
                event.getProduct().getName(),
                event.getProduct().getSku(),
                event.getProduct().getStockQuantity());
        notificationService.sendLowStockAlert(event.getProduct());
    }
}
