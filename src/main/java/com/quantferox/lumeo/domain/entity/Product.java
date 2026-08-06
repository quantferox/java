package com.quantferox.lumeo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products",
        indexes = {
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_slug",     columnList = "slug")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_product_sku", columnNames = "sku"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 150)
    private String slug;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(length = 2000)
    private String description;

    /**
     * Selling price - BigDecimal to avoid floating-point issues.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_price", precision = 12, scale = 2)
    private BigDecimal comparePrice;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private int stockQuantity = 0;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean featured = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // ── Convenience helpers ────────────────────────────────────────────────

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public boolean isOnSale() {
        return comparePrice != null && comparePrice.compareTo(price) > 0;
    }
}
