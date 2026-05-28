/**
 * DTO de solicitud para el primer paso del flujo de autenticación del marketplace UD.
 *
 * <p>Recibe las credenciales del usuario en el endpoint {@code POST /api/auth/login}.
 * Alineado con el diagrama ER: utiliza {@code correo_usuario} como identificador único
 * y {@code password_usua} para la contraseña en texto plano (comparada contra el hash bcrypt).
 *

 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    /** Identificador único de acceso del usuario (correo_usuario). */
    @NotBlank(message = "El correo de usuario es obligatorio")
    @Email(message = "Debe proporcionar un formato de correo electrónico válido")
    private String correoUsuario;

    /** Contraseña en texto plano (se compara contra el password_usua hash). */
    @NotBlank(message = "La contraseña es obligatoria")
    private String passwordUsua;
}
