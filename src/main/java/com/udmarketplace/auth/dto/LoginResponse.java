/**
 * DTO de respuesta emitido tras la verificación exitosa del código 2FA (Paso 2 del login).
 *
 * <p>Retornado por {@code POST /api/auth/verifyTwoFactor} cuando el código 2FA es válido.
 * Contiene el JWT de sesión de 24 horas y la información básica del usuario autenticado.
 *
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

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
