package com.udmarketplace.catalogo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de solicitud para el registro o actualización de un producto en el catálogo.
 * Solo accesible para usuarios con rol {@code VENDEDOR}.
 *
 * <p>La imagen del producto se envía como {@code MultipartFile} separado
 * en la misma solicitud {@code multipart/form-data}.
 *
 * @author Daniel Perez
 * @version 1.1
 * @since 2026-05-28
 */
@Data
public class CrearProductoRequest {

    /** Nombre del producto (obligatorio, máximo 150 caracteres). */
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombrePub;

    /** Descripción detallada del producto (opcional, máximo 500 caracteres). */
    @Size(max = 500)
    private String descripcionPub;

    /** Precio unitario del producto, debe ser mayor a cero (obligatorio). */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal precioPub;

    /** Ubicación geográfica del vendedor o del producto (máximo 200 caracteres). */
    @Size(max = 200)
    private String ubicacion;

    /** Condiciones de venta establecidas por el vendedor (máximo 500 caracteres). */
    @Size(max = 500)
    private String condicionesVenta;

    /** Identificador de la categoría activa a la que pertenece el producto (obligatorio). */
    @NotNull(message = "La categoría es obligatoria")
    private Long idCategoria;

    /** Indica si el producto está disponible para compra. Por defecto {@code true}. */
    private boolean disponibilidad = true;
}
