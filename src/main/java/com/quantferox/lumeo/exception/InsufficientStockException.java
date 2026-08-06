package com.quantferox.lumeo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int requested, int available) {
        super("Insufficient stock for '%s': requested %d, available %d"
                .formatted(productName, requested, available));
    }
}
