package com.quantferox.lumeo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super("%s not found with id: %d".formatted(resource, id));
    }

    public ResourceNotFoundException(String resource, String field, String value) {
        super("%s not found with %s: %s".formatted(resource, field, value));
    }
}
