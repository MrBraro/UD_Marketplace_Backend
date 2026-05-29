/**
 * DTO de solicitud para registrar una valoración sobre un producto comprado.
 *
 * <p>La calificación debe ser un entero entre 1 y 5 (REQ). La reseña predefinida
 * es opcional; si se omite {@code idResena}, la valoración se guarda sin texto de reseña.
 * La orden referenciada debe estar en estado CONFIRMADA y pertenecer al comprador
 * autenticado (REQ-17, REQ-18).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.valoracion.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CrearValoracionRequest {

    /** Identificador del producto a valorar (obligatorio). */
    @NotNull(message = "El producto es obligatorio")
    private Long idPub;

    /** Identificador de la orden de compra confirmada que habilita la valoración (obligatorio). */
    @NotNull(message = "La orden es obligatoria")
    private Long idOrden;

    /** Calificación numérica del producto, entre 1 (mínimo) y 5 (máximo) (obligatorio). */
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer calificacion;

    /** Identificador de la reseña predefinida seleccionada (opcional). */
    private Long idResena;
}
