package com.quantferox.lumeo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @NotBlank(message = "Street is required")
    private String shippingStreet;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String shippingCity;

    @Size(max = 100)
    private String shippingState;

    @NotBlank(message = "ZIP is required")
    @Size(max = 20)
    private String shippingZip;

    @NotBlank(message = "Country is required")
    @Size(max = 60)
    private String shippingCountry;

    @Size(max = 1000)
    private String notes;

    @Data
    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        private Long productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;
    }
}
