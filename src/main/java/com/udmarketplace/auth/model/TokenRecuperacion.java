package com.udmarketplace.auth.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un token único para recuperación de contraseña.
 *
 * <p>Mapeada a la tabla {@code token_recuperacion}. Cada token es:
 * <ul>
 *   <li>Único (UUID generado al momento de la solicitud)</li>
 *   <li>De uso único: una vez consumido, {@code usado} pasa a {@code true}</li>
 *   <li>De tiempo limitado: expira según {@code app.auth.minutos-expiry-token-recuperacion}</li>
 * </ul>
 *
 * <p>El flujo es:
 * <ol>
 *   <li>Usuario solicita recuperación → se crea este registro</li>
 *   <li>Python envía el token por correo</li>
 *   <li>Usuario usa el token → se valida y marca como {@code usado = true}</li>
 * </ol>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Entity
@Table(name = "token_recuperacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRecuperacion {

    /** Identificador único del token de recuperación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long idToken;

    /** Usuario propietario del token de recuperación. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_user", nullable = false)
    private User usuario;

    /** Valor del token (UUID). Único en toda la tabla. */
    @Column(name = "token", unique = true, nullable = false, length = 255)
    private String token;

    /** Fecha y hora de expiración del token. Después de este momento es inválido. */
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    /** {@code true} si el token ya fue utilizado para cambiar la contraseña. */
    @Column(name = "usado")
    private boolean usado;
}
