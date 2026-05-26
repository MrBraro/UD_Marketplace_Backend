package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response del endpoint POST /api/auth/verifyTwoFactor cuando el código 2FA es válido (RF11).
 *
 * <p>Contiene el JWT de sesión que el cliente debe incluir en el header
 * {@code Authorization: Bearer <token>} para todas las solicitudes protegidas.
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /** JWT de sesión con duración de 24 horas. */
    private String token;

    /** Username del usuario autenticado. */
    private String username;

    /** Rol del usuario (ADMIN, SELLER, BUYER). */
    private String role;

    /**
     * Tipo de token. Siempre {@code "Bearer"}.
     * El cliente debe construir el header como: {@code Authorization: Bearer <token>}.
     */
    private String tokenType;
}
