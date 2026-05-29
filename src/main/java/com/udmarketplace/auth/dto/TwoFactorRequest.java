/**
 * DTO de solicitud para el segundo paso del flujo de autenticación 2FA del marketplace UD.
 *
 * <p>Recibe el correo del usuario y el código numérico de 6 dígitos enviado al email
 * en el endpoint {@code POST /api/auth/verifyTwoFactor}. El código tiene una vigencia
 * máxima de 10 minutos desde su generación.
 *
 * @author
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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
