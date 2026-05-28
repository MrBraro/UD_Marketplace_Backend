package com.udmarketplace.pqr.model;

import com.udmarketplace.auth.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una interacción (mensaje) dentro de una PQR (REQ-14).
 *
 * <p>Mapea la tabla {@code interaccion_pqr}. Cada interacción registra quién envió
 * el mensaje, el contenido y la fecha/hora exacta, formando un historial cronológico
 * de la comunicación entre el usuario creador y el administrador asignado.
 *
 * <p>No se pueden agregar interacciones a PQRs en estado {@code CERRADA}.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Entity
@Table(name = "interaccion_pqr")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteraccionPqr {

    /** Identificador único auto-incremental de la interacción. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interaccion")
    private Long idInteraccion;

    /** PQR a la que pertenece esta interacción. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "radicado", nullable = false)
    private Pqr pqr;

    /** Usuario que envió el mensaje (puede ser el creador de la PQR o el administrador asignado). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_user", nullable = false)
    private User autor;

    /** Contenido del mensaje de la interacción (almacenado como TEXT). */
    @Lob
    @Column(name = "mensaje", nullable = false)
    private String mensaje;

    /** Fecha y hora en que se registró la interacción. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
}
