package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response del endpoint POST /api/auth/login cuando las credenciales son válidas (RF08).
 *
 * <p>Indica al cliente que el primer paso fue exitoso y que debe enviar
 * el código 2FA para completar el proceso de autenticación.
 */
@Data
@AllArgsConstructor
public class LoginStepResponse {

    /**
     * Estado del proceso de autenticación.
     * Valor esperado: {@code "TWO_FACTOR_REQUIRED"}.
     */
    private String step;

    /** Username del usuario que completó el primer paso. */
    private String username;

    /** Mensaje informativo para el cliente. */
    private String message;
}
