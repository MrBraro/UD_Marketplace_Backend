package com.udmarketplace.pqr.model;

import com.udmarketplace.auth.model.Administrador;
import com.udmarketplace.auth.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una Petición, Queja o Reclamo (PQR) del sistema UD Marketplace.
 *
 * <p>Mapea la tabla {@code pqr} de la base de datos MySQL. El número de radicado
 * es auto-incremental y sirve como identificador único irrepetible (REQ-10).
 *
 * <p>Comportamiento automático al crear:
 * <ul>
 *   <li>REQ-10: radicado asignado por AUTO_INCREMENT</li>
 *   <li>REQ-11: fecha y hora de creación registradas con {@code LocalDate.now()} y {@code LocalTime.now()}</li>
 *   <li>REQ-12: archivo adjunto validado y almacenado como BLOB (máx. 5 MB, imagen/PDF)</li>
 *   <li>REQ-13: administrador asignado automáticamente al de menor carga de PQRs abiertas</li>
 * </ul>
 *
 * <p>El acceso está restringido al usuario creador y al administrador asignado.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Entity
@Table(name = "pqr")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pqr {

    /** Número de radicado único auto-incremental de la PQR (REQ-10). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "radicado")
    private Long radicado;

    /** Usuario que creó la PQR. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_user", nullable = false)
    private User usuario;

    /** Tipo de PQR: PETICION, QUEJA o RECLAMO. */
    @Column(name = "tipo_pqr", length = 100)
    private String tipoPqr;

    /** Fecha de creación de la PQR (registrada automáticamente, REQ-11). */
    @Column(name = "fecha_creacion_pqr")
    private LocalDate fechaCreacionPqr;

    /** Hora de creación de la PQR (registrada automáticamente, REQ-11). */
    @Column(name = "hora_creacion_pqr")
    private LocalTime horaCreacionPqr;

    /** Archivo adjunto almacenado como BLOB (imagen/PDF, máx. 5 MB, REQ-12). */
    @Lob
    @Column(name = "imagen_pqr")
    private byte[] imagenPqr;

    /** Descripción detallada de la PQR (máximo 500 caracteres). */
    @Column(name = "descripcion_pqr", length = 500)
    private String descripcionPqr;

    /** Administrador asignado automáticamente con menor carga de PQRs abiertas (REQ-13). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_admin")
    private Administrador administrador;

    /** Estado actual de la PQR: ENVIADA, EN_PROCESO o CERRADA. */
    @Column(name = "estado_pqr", length = 50)
    private String estadoPqr;

    /** Historial de interacciones (mensajes) de la PQR ordenados por fecha (REQ-14). */
    @OneToMany(mappedBy = "pqr", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InteraccionPqr> interacciones = new ArrayList<>();
}
