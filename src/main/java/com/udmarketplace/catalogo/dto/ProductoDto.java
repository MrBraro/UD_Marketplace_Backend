package com.udmarketplace.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con el detalle completo de un producto del catálogo.
 *
 * <p>Incluye datos del vendedor, la categoría y la calificación promedio
 * calculada a partir de las valoraciones activas .
 * La imagen binaria no se incluye en este DTO para optimizar el rendimiento.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {

    /** Identificador único de la publicación. */
    private Long idPub;

    /** Nombre del producto visible en el catálogo. */
    private String nombrePub;

    /** Descripción detallada del producto. */
    private String descripcionPub;

    /** Precio de venta unitario del producto. */
    private BigDecimal precioPub;

    /** Ubicación geográfica del vendedor o del producto. */
    private String ubicacion;

    /** Condiciones de venta definidas por el vendedor. */
    private String condicionesVenta;

    /** Indica si el producto está disponible para compra. */
    private boolean disponibilidad;

    /** Indica si el producto está activo (false = eliminado lógicamente). */
    private boolean activoPub;

    /** Fecha y hora de registro del producto en el sistema. */
    private LocalDateTime fechaRegistro;

    /** Identificador de la categoría asociada al producto. */
    private Long idCategoria;

    /** Nombre de la categoría asociada al producto. */
    private String nombreCategoria;

    /** Identificador del vendedor propietario del producto. */
    private Long idVendedor;

    /** Nombre completo del vendedor (primer nombre + primer apellido). */
    private String nombreVendedor;

    /** Calificación promedio del producto calculada desde valoraciones activas (REQ-15). */
    private Double calificacionPromedio;
}
