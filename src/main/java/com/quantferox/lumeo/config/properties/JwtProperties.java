package com.quantferox.lumeo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Strongly-typed binding for {@code lumeo.jwt.*} properties.
 * Validated at startup - the app won't start with a missing secret.
 */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "lumeo.jwt")
public class JwtProperties {

    @NotBlank(message = "lumeo.jwt.secret must not be blank")
    private String secret;

    @Positive(message = "lumeo.jwt.expiration-ms must be positive")
    private long expirationMs = 86_400_000L;
}
