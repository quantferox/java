package com.quantferox.lumeo.dto.request;

import com.quantferox.lumeo.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;
}
