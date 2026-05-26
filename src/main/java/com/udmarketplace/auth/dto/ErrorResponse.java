package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response estándar para errores de la API.
 *
 * <p>Retornado por {@code GlobalExceptionHandler} en todos los casos de error.
 * El cliente siempre recibirá este formato cuando el HTTP status sea 4xx o 5xx.
 */
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
