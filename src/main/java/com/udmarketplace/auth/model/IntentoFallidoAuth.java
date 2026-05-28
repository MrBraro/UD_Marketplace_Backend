package com.udmarketplace.auth.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que registra cada intento fallido de autenticación en el sistema.
 *
 * <p>Mapeada a la tabla {@code intento_fallido_auth}. Cumple los requerimientos:
 * <ul>
 *   <li>registrar usuario intentado, fecha, hora e IP de origen</li>
 *   <li>base para contar intentos y decidir bloqueo temporal</li>
 * </ul>
 *
 * <p>El campo {@code exitoso} permite diferenciar intentos fallidos de accesos
 * exitosos en la auditoría, aunque solo se persisten los fallidos.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Entity
@Table(name = "intento_fallido_auth")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentoFallidoAuth {

    /** Identificador único del registro de intento. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intento")
    private Long idIntento;

    /** Correo electrónico utilizado en el intento (puede no existir en el sistema). */
    @Column(name = "correo_intentado", length = 150)
    private String correoIntentado;

    /** Dirección IP de origen de la solicitud (IPv4 o IPv6). */
    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;

    /** Fecha y hora exactas del intento de autenticación. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** {@code true} si el intento resultó exitoso; {@code false} si falló. */
    @Column(name = "exitoso")
    private boolean exitoso;
}
