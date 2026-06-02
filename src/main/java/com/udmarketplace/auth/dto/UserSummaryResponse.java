package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta resumida para listados administrativos de usuarios del marketplace UD.
 *
 * <p>Se utiliza en el endpoint {@code GET /api/admin/users} para exponer una vista
 * ligera del usuario, adecuada para tablas o listados en el panel administrativo,
 * sin incluir información sensible ni detalles innecesarios del perfil.
 *
 * <p>Este DTO permite desacoplar la respuesta del API respecto de la entidad
 * persistente {@code User}, devolviendo únicamente los atributos necesarios
 * para la gestión operativa de usuarios.
 *
 * @version 1.0
 * @since 2026-05-29
 */
@Data
@Builder
@AllArgsConstructor
public class UserSummaryResponse {

    /** Código único del usuario (clave primaria). */
    private Long codigoUsua;

    /** Correo institucional del usuario, usado como identificador de acceso. */
    private String correoUsuario;

    /** Rol asignado al usuario en el sistema. */
    private String rolUsua;

    /** Primer nombre del usuario. */
    private String primerNombre;

    /** Primer apellido del usuario. */
    private String primerApellido;

    /** Indica si la cuenta del usuario está activa. */
    private boolean activo;

    /** Indica si el usuario fue registrado como menor de edad. */
    private boolean menorEdad;
}