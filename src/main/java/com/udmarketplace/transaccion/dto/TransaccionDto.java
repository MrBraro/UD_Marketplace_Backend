package com.udmarketplace.transaccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con los datos completos de una transacción del marketplace UD.
 *
 * <p>Incluye los tres actores de la transacción (REQ-05): comprador, vendedor y producto,
 * así como el estado actual y el detalle de entrega (presente solo cuando la transacción
 * está en estado {@code CONFIRMADA}).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionDto {

    /** Identificador único de la orden de compra. */
    private Long idOrden;

    /** Identificador del comprador de la transacción. */
    private Long idComprador;

    /** Nombre completo del comprador. */
    private String nombreComprador;

    /** Identificador del vendedor de la transacción. */
    private Long idVendedor;

    /** Nombre completo del vendedor. */
    private String nombreVendedor;

    /** Identificador del producto transado. */
    private Long idProducto;

    /** Nombre del producto al momento de la transacción. */
    private String nombreProducto;

    /** Monto total de la transacción (precio del producto en el momento de la compra). */
    private BigDecimal totalCompra;

    /** Estado actual de la orden (PENDIENTE, CONFIRMADA, CANCELADA, ENTREGADA). */
    private String estadoOrden;

    /** Fecha y hora exacta en que se registró la transacción. */
    private LocalDateTime datetimeCompra;

    /** Detalle de entrega con snapshot del producto; presente solo en órdenes CONFIRMADAS. */
    private OrdenEntregaDto detalleEntrega;
}
