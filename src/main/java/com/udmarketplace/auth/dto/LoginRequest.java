package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body para el endpoint POST /api/auth/login.
 *
 * <p>Alineado con el diagrama ER: utiliza correo_usuario como identificador único
 * y password_usua para la contraseña.
 */
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
