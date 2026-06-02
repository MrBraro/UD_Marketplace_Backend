package com.udmarketplace.auth.repository;

import com.udmarketplace.auth.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad {@link AuditLog}.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
