/**
 * DTO de respuesta con los datos de una valoración registrada en el marketplace UD.
 *
 * <p>Incluye la identificación del producto, vendedor y comprador involucrados,
 * la calificación otorgada, el texto de la reseña predefinida seleccionada,
 * la fecha del registro y el estado lógico de la valoración .
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.valoracion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValoracionDto {

    /** Identificador único de la valoración. */
    private Long idVal;

    /** Identificador del producto valorado. */
    private Long idProducto;

    /** Nombre del producto valorado. */
    private String nombreProducto;

    /** Identificador del vendedor del producto. */
    private Long idVendedor;

    /** Nombre completo del vendedor del producto. */
    private String nombreVendedor;

    /** Identificador del comprador que emitió la valoración. */
    private Long idComprador;

    /** Nombre completo del comprador que emitió la valoración. */
    private String nombreComprador;

    /** Calificación numérica asignada al producto (1–5). */
    private Integer calificacion;

    /** Texto de la reseña predefinida seleccionada, o {@code null} si no se eligió ninguna. */
    private String resenaPredefinida;

    /** Fecha en que se registró la valoración. */
    private LocalDate fechaValo;

    /** Estado lógico: {@code true} = activa; {@code false} = inactivada (historial conservado). */
    private boolean estadoValo;
}
