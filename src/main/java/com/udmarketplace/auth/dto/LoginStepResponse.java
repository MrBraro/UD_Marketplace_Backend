/**
 * DTO de respuesta intermedia del flujo de autenticación en dos factores del marketplace UD (Paso 1).
 *
 * <p>Retornado por {@code POST /api/auth/login} cuando las credenciales son válidas pero
 * el segundo factor aún no ha sido verificado. Indica al cliente que debe solicitar el código 2FA.
 *
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

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
