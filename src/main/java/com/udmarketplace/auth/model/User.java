package com.udmarketplace.auth.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un usuario del sistema.
 *
 * <p>Contiene las credenciales de acceso, el rol para autorización (RF24)
 * y el campo para almacenar temporalmente el código 2FA enviado por email.
 *
 * <p>Decisión de diseño: el campo {@code twoFactorCode} se persiste en la DB
 * como almacenamiento temporal del código generado. Se limpia después de su
 * uso exitoso. En el futuro puede migrarse a Redis con TTL automático.
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
    private Long id;

    /** Identificador único de acceso del usuario (RF08). */
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    /** Dirección de email para envío del código 2FA (RF11). */
    @Column(nullable = false, length = 150)
    private String email;

    /** Contraseña almacenada como hash BCrypt (RF08). */
    @Column(nullable = false)
    private String password;

    /** Rol del usuario para autorización por rutas (RF24). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * Código 2FA generado y enviado al email del usuario.
     * Se establece al hacer login con credenciales válidas.
     * Se elimina (null) tras verificación exitosa.
     */
    @Column(length = 6)
    private String twoFactorCode;
}
