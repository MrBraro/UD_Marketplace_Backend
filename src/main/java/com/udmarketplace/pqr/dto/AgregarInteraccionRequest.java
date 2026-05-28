package com.udmarketplace.pqr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de solicitud para agregar un nuevo mensaje a una PQR existente (REQ-14).
 * No se puede enviar mensajes a PQRs en estado CERRADA.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
public class AgregarInteraccionRequest {

    /** Contenido del mensaje a agregar en la PQR (obligatorio). */
    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;
}
