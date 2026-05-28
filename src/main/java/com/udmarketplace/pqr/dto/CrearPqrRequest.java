package com.udmarketplace.pqr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de solicitud para la creación de una PQR (Petición, Queja o Reclamo).
 * El archivo adjunto se envía como {@code MultipartFile} separado en la misma
 * solicitud {@code multipart/form-data}.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
public class CrearPqrRequest {

    /** Tipo de PQR (obligatorio): debe ser PETICION, QUEJA o RECLAMO. */
    @NotNull(message = "El tipo de PQR es obligatorio")
    @Pattern(regexp = "PETICION|QUEJA|RECLAMO", message = "Tipo debe ser: PETICION, QUEJA o RECLAMO")
    private String tipoPqr;

    /** Descripción detallada de la solicitud (obligatorio, máximo 500 caracteres). */
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500)
    private String descripcionPqr;
}
