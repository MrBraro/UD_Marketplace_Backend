package com.udmarketplace.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de solicitud para la creación de una nueva categoría en el catálogo.
 * Solo usuarios con rol {@code ADMINISTRADOR} pueden enviar esta solicitud.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
public class CrearCategoriaRequest {

    /** Nombre único de la categoría (obligatorio, máximo 100 caracteres). */
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100)
    private String nombreCat;

    /** Descripción opcional de la categoría (máximo 500 caracteres). */
    @Size(max = 500)
    private String descripcionCat;
}
