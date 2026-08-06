package com.quantferox.lumeo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantferox.lumeo.domain.entity.AuditLog;
import com.quantferox.lumeo.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes audit records asynchronously so the main request thread is never blocked.
 *
 * Uses {@code Propagation.REQUIRES_NEW} - the audit record is committed
 * independently even if the main transaction rolls back.
 * This is critical: you must always know what was attempted, even on failure.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper       objectMapper;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId,
                    Object payload, String ipAddress) {
        String actor = resolveActor();
        String json  = toJson(payload);

        AuditLog entry = AuditLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .payload(json)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(entry);
        log.debug("[AUDIT] actor={} action={} entity={}#{}", actor, action, entityType, entityId);
    }

    /** Convenience overload without IP (for scheduled/system actions). */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSystem(String action, String entityType, Long entityId, Object payload) {
        log(action, entityType, entityId, payload, "SYSTEM");
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "ANONYMOUS";
    }

    private String toJson(Object payload) {
        if (payload == null) return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit payload: {}", e.getMessage());
            return payload.toString();
        }
    }
}
