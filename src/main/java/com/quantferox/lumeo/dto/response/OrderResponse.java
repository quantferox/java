package com.quantferox.lumeo.dto.response;

import com.quantferox.lumeo.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingCountry;
    private String notes;
    private Long userId;
    private String userFullName;
    private List<OrderItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;
}
