package com.udmarketplace.auth.service;

import com.udmarketplace.auth.model.AccionAuditoria;

/**
 * Servicio de auditoría del sistema.
 *
 * <p>Registra eventos críticos de forma inmutable en la
 * tabla de auditoría para trazabilidad y cumplimiento.</p>
 */
public interface AuditService {

    /**
     * Registra un evento de auditoría.
     *
     * @param accion       tipo de acción auditada
     * @param entidad      nombre de la entidad afectada
     * @param entidadId    ID de la entidad afectada
     * @param usuarioId    ID del usuario que ejecutó la acción
     * @param usuarioEmail email del usuario que ejecutó la acción
     * @param detalle      descripción del cambio realizado
     */
    void registrar(AccionAuditoria accion,
                   String entidad,
                   Long entidadId,
                   Long usuarioId,
                   String usuarioEmail,
                   String detalle);
}
