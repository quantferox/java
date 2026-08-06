package com.quantferox.lumeo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

/**
 * Strongly-typed binding for {@code lumeo.rate-limit.*} properties.
 * Values differ per profile (dev = generous, prod = strict).
 */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "lumeo.rate-limit")
public class RateLimitProperties {

    /** Max tokens in the bucket (= max burst). */
    @Positive
    private long capacity = 200;

    /** Tokens added per refill interval. */
    @Positive
    private long refillTokens = 200;

    /** Refill interval in seconds. */
    @Positive
    private long refillSeconds = 60;
}
