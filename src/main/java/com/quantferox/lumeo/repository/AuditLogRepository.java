package com.quantferox.lumeo.repository;

import com.quantferox.lumeo.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByActor(String actor, Pageable pageable);

    Page<AuditLog> findAllByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    List<AuditLog> findAllByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to);

    Page<AuditLog> findAllByAction(String action, Pageable pageable);
}
