/**
 * Entidad JPA que representa una reseña predefinida disponible para seleccionar
 * al registrar una valoración en el marketplace UD.
 *
 * <p>Mapea la tabla {@code resena_predefinida}. El catálogo de reseñas es
 * administrado por el equipo; las reseñas inactivas ({@code activo = false})
 * no se muestran a los compradores pero se conservan como historial.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.valoracion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resena_predefinida")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResenaPredefinida {

    /** Identificador único de la reseña predefinida (AUTO_INCREMENT). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Long idResena;

    /** Texto descriptivo de la reseña, visible para el comprador al valorar. */
    @Column(name = "texto_resena", nullable = false, length = 255)
    private String textoResena;

    /** Indica si la reseña está disponible para selección ({@code true}) o fue desactivada ({@code false}). */
    @Column(name = "activo")
    private boolean activo = true;
}
