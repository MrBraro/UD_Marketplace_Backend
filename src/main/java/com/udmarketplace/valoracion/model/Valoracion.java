/**
 * Entidad JPA que representa una valoración registrada en el marketplace UD.
 *
 * <p>Mapea la tabla {@code valoracion}. Cada valoración vincula un comprador,
 * un vendedor y un producto dentro de una transacción confirmada, preservando
 * el historial completo sin sobrescribir registros anteriores .
 * La relación comprador-vendedor queda registrada explícitamente,
 * y cada valoración puede llevar una reseña predefinida seleccionada.
 *
 * <p>Estado lógico: {@code estadoValo = true} indica valoración activa;
 * {@code false} indica valoración inactivada (historial conservado).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.valoracion.model;

import com.udmarketplace.auth.model.Comprador;
import com.udmarketplace.auth.model.Vendedor;
import com.udmarketplace.catalogo.model.Producto;
import com.udmarketplace.transaccion.model.Orden;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "valoracion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Valoracion {

    /** Identificador único de la valoración (AUTO_INCREMENT). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_val")
    private Long idVal;

    /** Texto de la reseña predefinida seleccionada, copiado como snapshot al momento de la valoración. */
    @Column(name = "valo_predefinida", length = 100)
    private String valoPredefinida;

    /** Calificación numérica entre 1 y 5 asignada al producto . */
    @Column(name = "calificacion")
    private Integer calificacion;

    /** Fecha en que se registró la valoración. */
    @Column(name = "fecha_valo")
    private LocalDate fechaValo;

    /**
     * Estado lógico de la valoración. {@code true} = activa (incluida en cálculos
     * de promedio y reputación); {@code false} = inactivada (historial preservado,
     * REQ-17).
     */
    @Column(name = "estado_valo")
    private boolean estadoValo = true;

    /** Orden de compra confirmada que origina esta valoración . */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden")
    private Orden orden;

    /** Producto que está siendo valorado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pub")
    private Producto producto;

    /** Vendedor del producto valorado; registra la relación comprador-vendedor . */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_vendedor")
    private Vendedor vendedor;

    /** Comprador que emite la valoración; registra la relación comprador-vendedor. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_comprador")
    private Comprador comprador;

    /** Reseña predefinida seleccionada opcionalmente por el comprador. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resena")
    private ResenaPredefinida resenaPredefinida;
}
