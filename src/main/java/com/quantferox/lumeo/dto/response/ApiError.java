package com.quantferox.lumeo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error envelope for all REST API error responses.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private int status;
    private String error;
    private String message;
    private String path;
    @Builder.Default
    private Instant timestamp = Instant.now();
    private Map<String, String> fieldErrors;
}
