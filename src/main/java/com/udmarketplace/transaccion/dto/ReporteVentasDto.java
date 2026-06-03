package com.udmarketplace.transaccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta del report service para el reporte de ventas globales.
 * Mapeada desde {@code GET /api/v1/report/ventas}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentasDto {
    private Integer totalOrdenes;
    private Double ingresosTotales;
    private String periodoDe;
    private String periodoHasta;
}
