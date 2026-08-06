package com.quantferox.lumeo.metrics;

import com.quantferox.lumeo.domain.enums.OrderStatus;
import com.quantferox.lumeo.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Custom Micrometer metrics exposed at {@code /actuator/prometheus} (or /actuator/metrics).
 *
 * Metrics registered:
 * <ul>
 *   <li>{@code lumeo.orders.placed}   - Counter, tagged by status</li>
 *   <li>{@code lumeo.revenue.total}   - Gauge, total delivered revenue in USD</li>
 *   <li>{@code lumeo.order.placement.time} - Timer for order placement latency</li>
 * </ul>
 *
 * No manual HTTP scraping needed - Micrometer + Actuator handle publishing.
 * In prod plug in Prometheus + Grafana for dashboards.
 */
@Slf4j
@Component
public class LumeoMetrics {

    // ── Counters ──────────────────────────────────────────────────────────

    private final Counter orderPlacedCounter;
    private final Counter orderCancelledCounter;
    private final Counter orderDeliveredCounter;

    // ── Timers ────────────────────────────────────────────────────────────

    public final Timer orderPlacementTimer;

    // ── Gauges (backed by AtomicReference for thread-safety) ─────────────

    private final AtomicReference<Double> revenueRef = new AtomicReference<>(0.0);

    public LumeoMetrics(MeterRegistry registry, OrderRepository orderRepository) {

        // Counters - increment manually when events occur
        this.orderPlacedCounter = Counter.builder("lumeo.orders.placed")
                .description("Total number of orders placed")
                .tag("status", "PLACED")
                .register(registry);

        this.orderCancelledCounter = Counter.builder("lumeo.orders.cancelled")
                .description("Total number of orders cancelled")
                .tag("status", "CANCELLED")
                .register(registry);

        this.orderDeliveredCounter = Counter.builder("lumeo.orders.delivered")
                .description("Total number of orders delivered")
                .tag("status", "DELIVERED")
                .register(registry);

        // Gauge - reads live from DB (polled by registry on scrape)
        Gauge.builder("lumeo.revenue.total", orderRepository,
                        repo -> {
                            try {
                                BigDecimal revenue = repo.sumTotalByStatus(OrderStatus.DELIVERED);
                                return revenue != null ? revenue.doubleValue() : 0.0;
                            } catch (Exception e) {
                                log.warn("Failed to read revenue gauge: {}", e.getMessage());
                                return 0.0;
                            }
                        })
                .description("Total revenue from delivered orders (USD)")
                .baseUnit("USD")
                .register(registry);

        // Timer - wrap order placement calls
        this.orderPlacementTimer = Timer.builder("lumeo.order.placement.time")
                .description("Time taken to place an order")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    // ── Public increment methods called by event listeners ────────────────

    public void incrementOrdersPlaced()    { orderPlacedCounter.increment(); }
    public void incrementOrdersCancelled() { orderCancelledCounter.increment(); }
    public void incrementOrdersDelivered() { orderDeliveredCounter.increment(); }
}
