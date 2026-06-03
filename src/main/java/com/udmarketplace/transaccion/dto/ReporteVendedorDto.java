package com.udmarketplace.transaccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta del report service con métricas de un vendedor específico.
 * Mapeada desde {@code GET /api/v1/report/vendedor/{id}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVendedorDto {
    private Long idVendedor;
    private Integer totalVentas;
    private Double ingresosTotales;
    private Double calificacionPromedio;
}
