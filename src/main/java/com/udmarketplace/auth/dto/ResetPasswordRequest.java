package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de solicitud para el cambio de contraseña mediante token de recuperación.
 *
 * <p>Requiere el token UUID enviado al correo del usuario y la nueva contraseña.
 * El token se valida contra expiración y uso previo antes de permitir el cambio.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
public class ResetPasswordRequest {

    /** Token UUID de recuperación recibido por correo electrónico. */
    @NotBlank(message = "El token es obligatorio")
    private String token;

    /** Nueva contraseña del usuario (mínimo 8 caracteres). */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String nuevaPassword;
}
