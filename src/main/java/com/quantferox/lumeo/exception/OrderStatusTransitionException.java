package com.quantferox.lumeo.exception;

import com.quantferox.lumeo.domain.enums.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class OrderStatusTransitionException extends RuntimeException {

    public OrderStatusTransitionException(OrderStatus from, OrderStatus to) {
        super("Invalid order status transition: %s → %s".formatted(from, to));
    }
}
