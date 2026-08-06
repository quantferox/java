package com.quantferox.lumeo.service;

import com.quantferox.lumeo.domain.enums.OrderStatus;
import com.quantferox.lumeo.dto.response.OrderResponse;
import com.quantferox.lumeo.dto.response.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Notification stub - logs messages that would be sent via SMTP / SMS / Slack.
 *
 * In a real project inject JavaMailSender, Twilio, or a message-queue producer here.
 * The service layer and event listeners don't change - only this class gets a real impl.
 */
@Slf4j
@Service
public class NotificationService {

    @Async
    public void sendOrderConfirmation(OrderResponse order) {
        log.info("[NOTIFICATION] Order confirmation → {} | order={} | total={}",
                order.getUserFullName(),
                order.getOrderNumber(),
                order.getTotalAmount());
        // TODO: inject JavaMailSender and send HTML email template
    }

    @Async
    public void sendStatusUpdateNotification(OrderResponse order, OrderStatus newStatus) {
        log.info("[NOTIFICATION] Status update → {} | order={} | status={}",
                order.getUserFullName(),
                order.getOrderNumber(),
                newStatus);
        // TODO: push notification / email depending on status
    }

    @Async
    public void sendLowStockAlert(ProductResponse product) {
        log.warn("[NOTIFICATION] Low stock alert → product='{}' | sku={} | qty={}",
                product.getName(),
                product.getSku(),
                product.getStockQuantity());
        // TODO: send to procurement Slack channel / email
    }
}
