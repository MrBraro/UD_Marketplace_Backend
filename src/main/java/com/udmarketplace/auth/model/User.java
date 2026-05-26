package com.udmarketplace.auth.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entidad que representa un usuario del sistema.
 *
 * <p>Mapeada exactamente para cumplir con los atributos y relaciones del
 * diccionario de datos técnico del Marketplace:
 * <ul>
 *   <li>codigo_user (clave primaria autonumerada)</li>
 *   <li>primer_nombre</li>
 *   <li>segundo_nombre</li>
 *   <li>primer_apellido</li>
 *   <li>segundo_apellido</li>
 *   <li>tel_user</li>
 *   <li>fecha_nacimiento</li>
 *   <li>correo_institu (identificador de acceso único y destino del código 2FA)</li>
 *   <li>activo (estado del usuario en la plataforma)</li>
 *   <li>perimiso_user (nivel de accesos / rol en el sistema)</li>
 *   <li>genero</li>
 *   <li>password_usua (hash BCrypt de contraseña, necesario para el RF08)</li>
 * </ul>
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_user")
    private Long codigoUser;

    @Column(name = "primer_nombre", nullable = false, length = 50)
    private String primerNombre;

    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @Column(name = "primer_apellido", nullable = false, length = 50)
    private String primerApellido;

    @Column(name = "segundo_apellido", length = 50)
    private String segundoApellido;

    @Column(name = "tel_user", nullable = false, length = 20)
    private String telUser;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "correo_institu", unique = true, nullable = false, length = 150)
    private String correoInstitu;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Enumerated(EnumType.STRING)
    @Column(name = "perimiso_user", nullable = false, length = 50)
    private Role perimisoUser;

    @Column(name = "genero", nullable = false, length = 20)
    private String genero;

    @Column(name = "password_usua", nullable = false)
    private String passwordUsua;

    /**
     * Código 2FA generado y enviado al correo institucional.
     */
    @Column(name = "two_factor_code", length = 6)
    private String twoFactorCode;
}
