package com.udmarketplace.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para exponer información de una categoría.
 *
 * <p>Se utiliza en las respuestas de la API para mostrar los datos
 * relevantes de la categoría sin exponer detalles internos innecesarios.
 *
 * <p>Incluye el estado activo/inactivo, contador de productos y el
 * administrador responsable del registro cuando aplique.
 *
 * @author Daniel Perez
 * @version 1.1
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

    /** Fecha y hora en que fue registrada */
    private LocalDateTime fechaRegistro;

    /** Identificador del administrador que creó la categoría. */
    private Long codigoAdmin;

    /** Nombre completo del administrador, si deseas mostrarlo en la UI. */
    private String nombreAdmin;
}
