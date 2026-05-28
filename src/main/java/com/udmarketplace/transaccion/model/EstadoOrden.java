package com.udmarketplace.transaccion.model;

/**
 * Enumeración de los estados posibles de una orden de compra en el marketplace UD.
 *
 * <p>Ciclo de transición de estados:
 * <pre>
 *   PENDIENTE → CONFIRMADA → ENTREGADA
 *   PENDIENTE → CANCELADA
 *   CONFIRMADA → CANCELADA
 * </pre>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public enum EstadoOrden {

    /** Intención de compra registrada por el comprador, en espera de confirmación del vendedor. */
    PENDIENTE,

    /** Transacción confirmada por el vendedor; se genera automáticamente la orden de entrega. */
    CONFIRMADA,

    /** Orden cancelada por el comprador o el vendedor. */
    CANCELADA,

    /** Entrega del producto completada. */
    ENTREGADA
}
