package com.quantferox.lumeo.scheduler;

import com.quantferox.lumeo.domain.enums.OrderStatus;
import com.quantferox.lumeo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Daily KPI report - runs at 07:00 every day.
 *
 * In production this would write to a reporting DB, push to Datadog,
 * or email a PDF. Here it logs the summary - plug in your sink.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final OrderRepository orderRepository;

    /**
     * Cron: every day at 07:00 server time.
     * Change timezone via {@code zone} attribute if needed.
     */
    @Scheduled(cron = "${lumeo.scheduler.daily-report-cron:0 0 7 * * *}")
    public void dailyReport() {
        log.info("[REPORT] ── Daily KPI Report ──────────────────────────");

        List<Object[]> counts = orderRepository.countByStatus();
        counts.forEach(row -> log.info("[REPORT]  Orders {}: {}", row[0], row[1]));

        BigDecimal revenue = orderRepository.sumTotalByStatus(OrderStatus.DELIVERED);
        log.info("[REPORT]  Total delivered revenue: ${}", revenue);

        log.info("[REPORT] ────────────────────────────────────────────────");

        // TODO: format as HTML, inject JavaMailSender, send to finance team
    }
}
