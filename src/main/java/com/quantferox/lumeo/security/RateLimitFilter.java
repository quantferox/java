package com.quantferox.lumeo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantferox.lumeo.config.properties.RateLimitProperties;
import com.quantferox.lumeo.dto.response.ApiError;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token-bucket rate limiter per client IP using Bucket4j.
 *
 * Bucket4j handles all the token-bucket math internally - no custom logic needed.
 * In a multi-node deployment, swap the in-memory map for a distributed bucket
 * backed by Redis or Hazelcast (Bucket4j supports both natively via
 * {@code bucket4j-redis} or {@code bucket4j-hazelcast} adapters).
 *
 * Applies only to {@code /api/**}.
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;
    private final ObjectMapper        objectMapper;

    /** One bucket per client IP - Bucket4j is thread-safe. */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties props, ObjectMapper objectMapper) {
        this.props        = props;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only rate-limit API requests
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest  request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain         chain)
            throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        Bucket bucket   = buckets.computeIfAbsent(clientIp, this::newBucket);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("[RATE LIMIT] Blocked request from ip={} path={}",
                    clientIp, request.getRequestURI());
            rejectRequest(response, clientIp);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(props.getCapacity())
                .refillGreedy(props.getRefillTokens(),
                        Duration.ofSeconds(props.getRefillSeconds()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private void rejectRequest(HttpServletResponse response, String ip) throws IOException {
        ApiError error = ApiError.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Too Many Requests")
                .message("Rate limit exceeded. Please slow down.")
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    private String resolveClientIp(HttpServletRequest request) {
        // Respect X-Forwarded-For from reverse proxy (nginx / AWS ALB)
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
