package com.udmarketplace.transaccion.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO con los filtros opcionales para la consulta del historial de transacciones.
 *
 * <p>Todos los campos son opcionales. El servicio construye la consulta JPQL
 * incluyendo únicamente los predicados de los campos no nulos.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
public class FiltroHistorialRequest {

    /** Filtro por identificador del comprador (nullable). */
    private Long codigoComprador;

    /** Filtro por identificador del vendedor (nullable). */
    private Long codigoVendedor;

    /** Filtro por estado de la orden: PENDIENTE, CONFIRMADA, CANCELADA o ENTREGADA (nullable). */
    private String estado;

    /** Fecha/hora de inicio del rango de búsqueda por fecha de compra (nullable, inclusivo). */
    private LocalDateTime desde;

    /** Fecha/hora de fin del rango de búsqueda por fecha de compra (nullable, inclusivo). */
    private LocalDateTime hasta;
}
