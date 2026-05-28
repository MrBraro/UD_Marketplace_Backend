package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de solicitud para el inicio del flujo de recuperación de contraseña.
 *
 * <p>Recibe el correo electrónico del usuario que desea recuperar su contraseña.
 * El sistema genera un token de recuperación y lo envía al correo indicado
 * a través del backend Python.
 *
 * <p>La respuesta nunca revela si el correo existe o no en el sistema,
 * para prevenir la enumeración de usuarios.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
public class RecuperarPasswordRequest {

    /** Correo electrónico del usuario que solicita la recuperación de contraseña. */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correoUsuario;
}
