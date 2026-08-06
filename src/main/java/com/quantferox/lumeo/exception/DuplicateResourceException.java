package com.quantferox.lumeo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resource, String field, String value) {
        super("%s already exists with %s: %s".formatted(resource, field, value));
    }
}
