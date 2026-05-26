package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body para el endpoint POST /api/auth/verifyTwoFactor (RF11).
 *
 * <p>Contiene el username y el código de 6 dígitos recibido por email.
 * Si ambos son válidos, el backend emite el JWT de sesión.
 */
@Data
public class TwoFactorRequest {

    /** Nombre de usuario que inició el proceso de login. */
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    /** Código de 6 dígitos enviado al email del usuario. */
    @NotBlank(message = "El código de verificación es obligatorio")
    private String twoFactorCode;
}
