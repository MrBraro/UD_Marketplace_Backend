package com.udmarketplace.transaccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con los datos del detalle de una orden de entrega.
 *
 * <p>Contiene el snapshot inmutable del producto capturado al momento de la confirmación
 * de la transacción, junto con el código de confirmación digital único.
 * No incluye la imagen binaria para optimizar el rendimiento de la respuesta.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenEntregaDto {

    /** Identificador único del detalle de entrega. */
    private Long idDetalle;

    /** Nombre del producto capturado al momento de la confirmación (snapshot). */
    private String nombreProducto;

    /** Descripción del producto capturada al momento de la confirmación (snapshot). */
    private String descripcionProd;

    /** Precio unitario del producto al momento de la transacción (snapshot). */
    private BigDecimal precioUnitario;

    /** Fecha y hora en que se generó la orden de entrega. */
    private LocalDateTime fechaGeneracion;

    /** Código de confirmación digital único en formato {@code CONF-{idOrden}-{UUID8}}. */
    private String confirmacionDigital;
}
