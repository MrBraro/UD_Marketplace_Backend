package com.udmarketplace.catalogo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de búsqueda con filtros dinámicos para consultar productos del catálogo.
 *
 * <p>Todos los campos son opcionales. El servicio construye la consulta JPA dinámica
 * incluyendo únicamente los predicados correspondientes a los campos no nulos.
 *
 * <p>Ordenamiento soportado mediante el campo {@code ordenarPor}:
 * <ul>
 *   <li>{@code precio_asc}  — precio ascendente</li>
 *   <li>{@code precio_desc} — precio descendente</li>
 *   <li>{@code nombre}      — nombre alfabético ascendente</li>
 *   <li>Cualquier otro valor o null — fecha de registro descendente (más recientes primero)</li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
public class FiltroProductoRequest {

    /** Texto de búsqueda en el nombre del producto (búsqueda parcial, insensible a mayúsculas). */
    private String nombre;

    /** Identificador de la categoría por la que filtrar. */
    private Long idCategoria;

    /** Precio mínimo del rango de búsqueda (inclusivo). */
    private BigDecimal precioMin;

    /** Precio máximo del rango de búsqueda (inclusivo). */
    private BigDecimal precioMax;

    /** Calificación promedio mínima del producto (campo de filtro futuro, no implementado en Spec aún). */
    private Double calificacionMin;

    /** Texto de búsqueda en la ubicación del producto (búsqueda parcial). */
    private String ubicacion;

    /** Si es {@code true} filtra productos disponibles; {@code false} los no disponibles; {@code null} no filtra. */
    private Boolean disponibilidad;

    /** Criterio de ordenamiento del resultado (ver valores soportados en la descripción de la clase). */
    private String ordenarPor;
}
