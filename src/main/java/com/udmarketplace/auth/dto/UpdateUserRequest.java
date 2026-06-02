package com.udmarketplace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para la solicitud de actualización de datos de un usuario por un administrador.
 *
 * <p>Todos los campos son opcionales — solo se actualizan los que se envían
 * con valor no nulo. Esto permite actualizaciones parciales.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-06-01
 */
@Data
public class UpdateUserRequest {

    /** Nuevo primer nombre del usuario. */
    @Size(min = 1, max = 100, message = "El primer nombre debe tener entre 1 y 100 caracteres")
    private String primerNombre;

    /** Nuevo segundo nombre del usuario. */
    @Size(max = 100, message = "El segundo nombre no puede exceder 100 caracteres")
    private String segundoNombre;

    /** Nuevo primer apellido del usuario. */
    @Size(min = 1, max = 100, message = "El primer apellido debe tener entre 1 y 100 caracteres")
    private String primerApellido;

    /** Nuevo segundo apellido del usuario. */
    @Size(max = 100, message = "El segundo apellido no puede exceder 100 caracteres")
    private String segundoApellido;

    /** Nuevo correo electrónico institucional. */
    @Email(message = "El correo debe ser válido")
    private String correoUsuario;

    /** Nuevo teléfono de contacto. */
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telUser;

    /** Nuevo género del usuario. */
    @Size(max = 20, message = "El género no puede exceder 20 caracteres")
    private String genero;

    /** Nuevo estado de la cuenta (activa/inactiva). */
    private Boolean activo;
}
