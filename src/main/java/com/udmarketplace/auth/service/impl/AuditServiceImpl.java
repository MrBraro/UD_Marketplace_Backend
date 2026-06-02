package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.model.AccionAuditoria;
import com.udmarketplace.auth.model.AuditLog;
import com.udmarketplace.auth.repository.AuditLogRepository;
import com.udmarketplace.auth.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de auditoría.
 *
 * <p>Registra cada evento en la tabla {@code audit_log}
 * usando una transacción independiente para garantizar
 * que el registro persista incluso si la operación
 * principal falla después.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(AccionAuditoria accion,
                          String entidad,
                          Long entidadId,
                          Long usuarioId,
                          String usuarioEmail,
                          String detalle) {

        AuditLog entry = AuditLog.builder()
                .accion(accion)
                .entidad(entidad)
                .entidadId(entidadId)
                .usuarioId(usuarioId)
                .usuarioEmail(usuarioEmail)
                .detalle(detalle)
                .build();

        auditLogRepository.save(entry);

        log.info("AUDIT | {} | {} id={} | por {} | {}",
                accion, entidad, entidadId, usuarioEmail, detalle);
    }
}
