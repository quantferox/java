package com.quantferox.lumeo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private int stockQuantity;
    private String imageUrl;
    private boolean active;
    private boolean featured;
    private boolean inStock;
    private boolean onSale;
    private Long categoryId;
    private String categoryName;
    private Instant createdAt;
    private Instant updatedAt;
}
