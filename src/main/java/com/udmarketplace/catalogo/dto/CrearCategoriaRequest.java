package com.udmarketplace.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de solicitud para crear una nueva categoría.
 *
 * <p>Se utiliza en el registro de categorías por parte de usuarios con rol
 * administrador. Contiene los datos mínimos de negocio para cumplir RF28.
 *
 * <p>La descripción es opcional, pero el nombre es obligatorio porque identifica
 * de forma legible la categoría en el catálogo.
 *
 * @author Daniel Perez
 * @version 1.1
 * @since 2026-05-28
 */
@Data
public class CrearCategoriaRequest {

    /** Nombre único de la categoría (obligatorio, máximo 100 caracteres). */
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100)
    private String nombreCat;

    /** Descripción opcional de la categoría (máximo 500 caracteres). */
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcionCat;
}
