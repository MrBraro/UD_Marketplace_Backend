package com.udmarketplace.auth.model;

/**
 * Enumeración de acciones auditables en el sistema.
 *
 * <p>Define los tipos de eventos que se registran en la
 * tabla de auditoría para trazabilidad.</p>
 */
public enum AccionAuditoria {

    /** Se creó un nuevo usuario en el sistema. */
    USUARIO_CREADO,

    /** Se modificaron los datos de un usuario existente. */
    USUARIO_MODIFICADO,

    /** Se cambió el estado de una transacción/orden. */
    TRANSACCION_ESTADO_CAMBIADO,

    /** Se cerró una PQR. */
    PQR_CERRADA
}
