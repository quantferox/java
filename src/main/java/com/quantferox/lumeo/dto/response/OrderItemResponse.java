package com.quantferox.lumeo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
