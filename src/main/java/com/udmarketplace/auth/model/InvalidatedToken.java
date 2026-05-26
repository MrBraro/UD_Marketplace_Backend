package com.udmarketplace.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un token JWT invalidado (blacklisted).
 *
 * <p>Implementa la invalidación de tokens para logout (RF13, RF25).
 * Cuando un usuario hace logout, el token se persiste aquí y el filtro
 * JWT rechaza cualquier solicitud que use un token presente en esta tabla.
 *
 * <p>Decisión de diseño: se usa la misma base de datos relacional por
 * simplicidad. En un entorno de alta carga se puede migrar a Redis con TTL
 * igual a la expiración del JWT (24h), sin cambiar la interfaz de servicio.
 */
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
