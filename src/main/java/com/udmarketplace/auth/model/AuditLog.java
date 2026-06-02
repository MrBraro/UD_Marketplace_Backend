package com.udmarketplace.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA que registra eventos de auditoría del sistema.
 *
 * <p>Almacena un log inmutable de acciones críticas como
 * creación/modificación de usuarios, cambios de estado en
 * transacciones y cierres de PQR.</p>
 */
@Entity
@Table(name = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /** Clave primaria auto-generada. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tipo de acción auditada. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AccionAuditoria accion;

    /** Nombre de la entidad afectada (e.g. "User", "Orden", "Pqr"). */
    @Column(nullable = false, length = 100)
    private String entidad;

    /** Identificador de la entidad afectada. */
    @Column(name = "entidad_id")
    private Long entidadId;

    /** ID del usuario que realizó la acción. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Email del usuario que realizó la acción. */
    @Column(name = "usuario_email", length = 255)
    private String usuarioEmail;

    /** Detalle adicional sobre la acción realizada. */
    @Column(columnDefinition = "TEXT")
    private String detalle;

    /** Fecha y hora del evento de auditoría. */
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /** Inicializa la fecha de creación antes de persistir. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
