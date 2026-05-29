package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de solicitud para el paso 2 del flujo de recuperación de contraseña.
 *
 * <p>Recibe el token de recuperación enviado por correo y la nueva contraseña
 * que el usuario desea establecer. El token debe ser válido, no haber sido
 * usado previamente y no estar expirado.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
public class ResetPasswordRequest {

    /** Token UUID de recuperación recibido en el correo electrónico. */
    @NotBlank(message = "El token es obligatorio")
    private String token;

    /** Nueva contraseña deseada por el usuario (mínimo 8 caracteres). */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String nuevaPassword;
}
