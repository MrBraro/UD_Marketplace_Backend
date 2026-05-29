package com.udmarketplace.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta con la información de una categoría del catálogo.
 *
 * <p>Expone los datos públicos de la categoría sin información sensible de la base de datos
 * (no incluye la entidad del administrador ni claves foráneas directas).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDto {

    /** Identificador único de la categoría. */
    private Long idCategoria;

    /** Nombre descriptivo de la categoría. */
    private String nombreCat;

    /** Descripción opcional de la categoría. */
    private String descripcionCat;

    /** Indica si la categoría está activa en el catálogo. */
    private boolean activoCat;

    /** Cantidad de publicaciones activas asociadas a esta categoría (REQ-04). */
    private int contadorProductos;
}
