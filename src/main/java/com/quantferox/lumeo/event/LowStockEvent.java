package com.quantferox.lumeo.event;

import com.quantferox.lumeo.dto.response.ProductResponse;
import org.springframework.context.ApplicationEvent;

/**
 * Published when a product's stock falls at or below the low-stock threshold (5).
 * Listeners can send alerts to procurement / ops teams.
 */
public class LowStockEvent extends ApplicationEvent {

    private final ProductResponse product;

    public LowStockEvent(Object source, ProductResponse product) {
        super(source);
        this.product = product;
    }

    public ProductResponse getProduct() {
        return product;
    }
}
