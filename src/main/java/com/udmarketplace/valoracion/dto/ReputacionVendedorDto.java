/**
 * DTO de respuesta con el resumen de reputación de un vendedor del marketplace UD.
 *
 * <p>Consolida la calificación promedio de todas sus valoraciones activas ,
 * el total de reseñas positivas (calificación ≥ 4)y el total de valoraciones
 * activas recibidas.
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputacionVendedorDto {

    /** Identificador del vendedor. */
    private Long idVendedor;

    /** Nombre completo del vendedor. */
    private String nombreVendedor;

    /** Promedio de las calificaciones activas recibidas, redondeado a dos decimales . */
    private Double calificacionPromedio;

    /** Cantidad de valoraciones activas con calificación ≥ 4 (reseñas positivas)*/
    private long totalResenasPositivas;

    /** Total de valoraciones activas recibidas por el vendedor. */
    private long totalValoraciones;
}
