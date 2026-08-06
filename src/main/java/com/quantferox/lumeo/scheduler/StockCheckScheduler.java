package com.quantferox.lumeo.scheduler;

import com.quantferox.lumeo.dto.response.ProductResponse;
import com.quantferox.lumeo.event.LowStockEvent;
import com.quantferox.lumeo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodic low-stock sweep - runs every 30 minutes.
 *
 * Complements the real-time {@link LowStockEvent} published on stock adjustment:
 * catches cases where stock was already low at startup or changed via direct DB update.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockCheckScheduler {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductService           productService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Runs every 30 minutes.
     * {@code fixedDelay} means 30 min after the previous run completes,
     * not every 30 min on the clock - prevents overlap on slow DB.
     */
    @Scheduled(fixedDelayString = "${lumeo.scheduler.stock-check-ms:1800000}",
               initialDelayString = "${lumeo.scheduler.stock-check-initial-ms:60000}")
    public void checkLowStock() {
        log.debug("[SCHEDULER] Running low-stock check...");
        List<ProductResponse> lowStock = productService.findLowStock(LOW_STOCK_THRESHOLD);

        if (lowStock.isEmpty()) {
            log.debug("[SCHEDULER] No low-stock products found.");
            return;
        }

        log.warn("[SCHEDULER] Found {} low-stock product(s):", lowStock.size());
        lowStock.forEach(p -> {
            log.warn("  - '{}' (sku={}) qty={}", p.getName(), p.getSku(), p.getStockQuantity());
            eventPublisher.publishEvent(new LowStockEvent(this, p));
        });
    }
}
