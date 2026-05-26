package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body para el endpoint POST /api/auth/login (RF08).
 *
 * <p>Contiene las credenciales del usuario para validación inicial.
 * Si son válidas, se dispara el envío del código 2FA al email.
 */
@Data
public class LoginRequest {

    /** Identificador único de acceso del usuario. */
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    /** Contraseña en texto plano (se compara contra el hash BCrypt almacenado). */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
