/**
 * Entidad JPA que representa un token JWT invalidado (lista negra) en el marketplace UD.
 *
 * <p>Mapea la tabla {@code invalidated_tokens}. Cuando un usuario realiza logout,
 * el token activo se persiste aquí; el filtro {@link com.udmarketplace.auth.security.JwtFilter}
 * rechaza cualquier solicitud que use un token presente en esta tabla.
 *
 * <p>Decisión de diseño: se usa la misma base de datos relacional por simplicidad.
 * Puede migrarse a Redis con TTL igual a la expiración del JWT para mejor rendimiento.
 *
 * @author
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invalidated_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidatedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** El token JWT completo que fue invalidado. */
    @Column(nullable = false, length = 2048)
    private String token;

    /** Momento en que el token fue invalidado (para auditoría futura). */
    @Column(nullable = false)
    private LocalDateTime invalidatedAt;
}
