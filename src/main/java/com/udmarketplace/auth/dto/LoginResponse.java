package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response del endpoint POST /api/auth/verifyTwoFactor cuando el código 2FA es válido (Paso 2).
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /** JWT de sesión con duración de 24 horas. */
    private String token;

    /** Correo de usuario autenticado (correo_usuario). */
    private String correoUsuario;

    /** Rol del usuario (rol_usua: ADMIN, SELLER, BUYER). */
    private String rolUsua;

    /** Tipo de token. Siempre {@code "Bearer"}. */
    private String tokenType;
}
