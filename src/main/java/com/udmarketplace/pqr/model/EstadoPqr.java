package com.udmarketplace.pqr.model;

/**
 * Enumeración de los estados posibles de una PQR en el sistema UD Marketplace.
 *
 * <p>Transiciones permitidas:
 * <pre>
 *   ENVIADA → EN_PROCESO → CERRADA
 *   ENVIADA → CERRADA
 * </pre>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public enum EstadoPqr {

    /** PQR recién creada, pendiente de atención por el administrador asignado. */
    ENVIADA,

    /** PQR en proceso de gestión activa por el administrador. */
    EN_PROCESO,

    /** PQR cerrada; no acepta nuevas interacciones. Solo administradores pueden cerrarla. */
    CERRADA
}
