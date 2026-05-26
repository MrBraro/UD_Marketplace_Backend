package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response del endpoint POST /api/auth/login cuando las credenciales son válidas (Paso 1).
 */
@Data
@AllArgsConstructor
public class LoginStepResponse {

    /**
     * Estado del proceso de autenticación.
     * Valor esperado: {@code "TWO_FACTOR_REQUIRED"}.
     */
    private String step;

    /** Correo de usuario que completó el primer paso (correo_usuario). */
    private String correoUsuario;

    /** Mensaje informativo para el cliente. */
    private String message;
}
