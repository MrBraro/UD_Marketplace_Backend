/**
 * DTO de respuesta estándar para todos los errores de la API del marketplace UD.
 *
 * <p>Retornado por {@link com.udmarketplace.auth.exception.GlobalExceptionHandler}
 * en todos los casos de error (HTTP 4xx y 5xx). Garantiza que el cliente siempre
 * reciba un formato consistente sin exponer información interna del servidor.
 *
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** Código HTTP del error (ej: 400, 401, 403, 500). */
    private int status;

    /** Descripción del error. */
    private String message;

    /** Timestamp ISO-8601 del momento del error (UTC). */
    private String timestamp;
}
