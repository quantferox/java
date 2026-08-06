package com.quantferox.lumeo.dto.request;

import com.quantferox.lumeo.validation.OnCreate;
import com.quantferox.lumeo.validation.OnUpdate;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Name is required", groups = OnCreate.class)
    @Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    @NotBlank(message = "Slug is required", groups = OnCreate.class)
    @Size(max = 150)
    private String slug;

    // SKU cannot be changed on update - only required on create
    @NotBlank(message = "SKU is required", groups = OnCreate.class)
    @Null(message = "SKU cannot be changed after creation", groups = OnUpdate.class)
    @Size(max = 50)
    private String sku;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Price is required", groups = OnCreate.class)
    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @Digits(integer = 10, fraction = 2)
    private BigDecimal comparePrice;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stockQuantity;

    private String imageUrl;

    private boolean active = true;

    private boolean featured = false;

    @NotNull(message = "Category is required", groups = OnCreate.class)
    private Long categoryId;
}
