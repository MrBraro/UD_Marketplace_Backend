package com.udmarketplace.auth.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad base que representa un usuario del sistema UD Marketplace.
 *
 * <p>Implementa herencia JOINED para los subtipos {@link Administrador},
 * {@link Vendedor} y {@link Comprador}. Cada subtipo tiene su propia tabla
 * vinculada mediante clave foránea a {@code usuario}.
 *
 * <p>Campos mapeados al diccionario técnico de la base de datos:
 * <ul>
 *   <li>{@code codigo_user} — clave primaria auto-generada</li>
 *   <li>{@code correo_institu} — correo institucional, identificador único de acceso</li>
 *   <li>{@code password_hash} — contraseña almacenada con bcrypt</li>
 *   <li>{@code perimiso_user} — rol en el sistema (ADMINISTRADOR / VENDEDOR / COMPRADOR)</li>
 *   <li>{@code two_factor_code} — código 2FA temporal de 6 dígitos</li>
 *   <li>{@code two_factor_expiry} — fecha/hora de expiración del código 2FA</li>
 *   <li>{@code bloqueado_hasta} — bloqueo temporal por intentos fallidos </li>
 * </ul>
 *
 * 
 * @version 1.0
 * @since 2026-05-28
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Identificador único del usuario (PK auto-incremental). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_user")
    private Long codigoUsua;

    /** Primer nombre del usuario. */
    @Column(name = "primer_nomb", length = 100)
    private String primerNombre;

    /** Segundo nombre del usuario (opcional). */
    @Column(name = "segundo_nom", length = 100)
    private String segundoNombre;

    /** Primer apellido del usuario. */
    @Column(name = "primer_apel", length = 100)
    private String primerApellido;

    /** Segundo apellido del usuario (opcional). */
    @Column(name = "segundo_apel", length = 100)
    private String segundoApellido;

    /** Teléfono de contacto principal. */
    @Column(name = "tel_user", length = 20)
    private String telUser;

    /** Fecha de nacimiento del usuario. */
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    /** Correo institucional — identificador único de acceso al sistema. */
    @Column(name = "correo_institu", unique = true, nullable = false, length = 150)
    private String correoUsuario;

    /** Indica si la cuenta está habilitada para operar en el marketplace. */
    @Column(name = "activo", nullable = false)
    private boolean activo;

    /** Rol del usuario en el sistema (RBAC). */
    @Enumerated(EnumType.STRING)
    @Column(name = "perimiso_user", nullable = false, length = 50)
    private Role rolUsua;

    /** Género del usuario. */
    @Column(name = "genero", length = 20)
    private String genero;

    /** Contraseña almacenada como hash bcrypt. Nunca en texto plano. */
    @Column(name = "password_hash", nullable = false)
    private String passwordUsua;

    /** Código de verificación 2FA de 6 dígitos (temporal, se limpia tras uso). */
    @Column(name = "two_factor_code", length = 6)
    private String twoFactorCode;

    /**
     * Fecha y hora de expiración del código 2FA.
     * El código es inválido si este campo es anterior a la hora actual.
     */
    @Column(name = "two_factor_expiry")
    private LocalDateTime twoFactorExpiry;

    /**
     * Timestamp hasta el cual la cuenta está bloqueada temporalmente.
     * {@code null} indica que la cuenta no está bloqueada (REQ-03).
     */
    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;
}
