package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body para el endpoint POST /api/auth/verifyTwoFactor.
 *
 * <p>Contiene el correoUsuario y el código de 6 dígitos recibido.
 */
@Data
public class TwoFactorRequest {

    /** Correo de usuario que inició el proceso de login (correo_usuario). */
    @NotBlank(message = "El correo de usuario es obligatorio")
    @Email(message = "Debe proporcionar un formato de correo electrónico válido")
    private String correoUsuario;

    /** Código de 6 dígitos enviado al email del usuario. */
    @NotBlank(message = "El código de verificación es obligatorio")
    private String twoFactorCode;
}
