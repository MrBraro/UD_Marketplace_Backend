/**
 * DTO de respuesta con el perfil completo del usuario autenticado del marketplace UD.
 *
 * <p>Retornado por {@code GET /api/auth/me}. Expone todos los atributos del
 * diagrama ER que corresponden al perfil del usuario, excluyendo datos sensibles
 * como la contraseña y el código 2FA.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UserInfoResponse {

    /** Código único del usuario (clave primaria, {@code codigo_usua}). */
    private Long codigoUsua;

    /** Correo electrónico del usuario, usado como identificador de acceso. */
    private String correoUsuario;

    /** Rol asignado al usuario: ADMINISTRADOR, VENDEDOR o COMPRADOR. */
    private String rolUsua;

    /** Primer nombre del usuario ({@code primer_nombre}). */
    private String primerNombre;

    /** Segundo nombre del usuario ({@code segundo_nombre}), puede ser nulo. */
    private String segundoNombre;

    /** Primer apellido del usuario ({@code primer_apellido}). */
    private String primerApellido;

    /** Segundo apellido del usuario ({@code segundo_apellido}), puede ser nulo. */
    private String segundoApellido;

    /** Género del usuario ({@code genero}). */
    private String genero;

    /** Fecha de nacimiento del usuario ({@code fecha_nacimiento}). */
    private LocalDate fechaNacimiento;
}
