package com.udmarketplace.auth.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entidad que representa un usuario del sistema.
 *
 * <p>Mapeada exactamente para cumplir con los atributos y relaciones del
 * diagrama de Entidad-Relación (ER):
 * <ul>
 *   <li>correo_usuario (email / identificador de acceso único)</li>
 *   <li>password_usua (hash BCrypt de contraseña)</li>
 *   <li>codigo_usua (clave primaria autonumerada)</li>
 *   <li>nombre_usua (primer_nombre, segundo_nombre, primer_apellido, segundo_apellido)</li>
 *   <li>rol_usua (rol del usuario para autorización)</li>
 *   <li>fecha_nacimiento (fecha de nacimiento del usuario)</li>
 *   <li>genero (género del usuario)</li>
 * </ul>
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_usua")
    private Long codigoUsua;

    @Column(name = "primer_nombre", nullable = false, length = 50)
    private String primerNombre;

    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @Column(name = "primer_apellido", nullable = false, length = 50)
    private String primerApellido;

    @Column(name = "segundo_apellido", length = 50)
    private String segundoApellido;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_usua", nullable = false, length = 20)
    private Role rolUsua;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "genero", nullable = false, length = 20)
    private String genero;

    @Column(name = "correo_usuario", unique = true, nullable = false, length = 150)
    private String correoUsuario;

    @Column(name = "password_usua", nullable = false)
    private String passwordUsua;

    /**
     * Código 2FA generado y enviado al email del usuario.
     * Mantenido en persistencia para soportar el flujo de autenticación en dos pasos.
     */
    @Column(name = "two_factor_code", length = 6)
    private String twoFactorCode;
}
