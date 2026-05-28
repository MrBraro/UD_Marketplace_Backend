package com.udmarketplace.transaccion.model;

import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.model.Vendedor;
import com.udmarketplace.catalogo.model.Producto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una transacción (orden de compra) del marketplace UD.
 *
 * <p>Mapea la tabla {@code orden} de la base de datos MySQL. Cada orden asocia
 * al comprador, al vendedor y al producto transado.
 *
 * <p>Ciclo de vida del estado ({@link EstadoOrden}):
 * <ol>
 *   <li>{@code PENDIENTE}  — creada por el comprador (intención de compra)</li>
 *   <li>{@code CONFIRMADA} — confirmada por el vendedor; genera orden de entrega</li>
 *   <li>{@code ENTREGADA}  — entrega completada</li>
 *   <li>{@code CANCELADA}  — cancelada por cualquiera de las partes</li>
 * </ol>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Entity
@Table(name = "orden")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orden {

    /** Identificador único auto-incremental de la orden de compra. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long idOrden;

    /** Comprador que registra la intención de compra. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_user", nullable = false)
    private User comprador;

    /** Vendedor del producto transado. Se asigna automáticamente del producto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_vendedor")
    private Vendedor vendedor;

    /** Producto objeto de la transacción. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pub")
    private Producto producto;

    /** Monto total de la compra (igual al precio del producto al momento de la transacción). */
    @Column(name = "total_compra", precision = 12, scale = 2)
    private BigDecimal totalCompra;

    /** Estado actual de la orden (PENDIENTE, CONFIRMADA, CANCELADA, ENTREGADA). */
    @Column(name = "estado_orden", length = 50)
    private String estadoOrden;

    /** Fecha de la compra (solo fecha, sin hora). */
    @Column(name = "fecha_compr")
    private LocalDate fechaCompr;

    /** Fecha y hora exacta del registro de la transacción. */
    @Column(name = "datetime_compra")
    private LocalDateTime datetimeCompra;

    /** Detalle de entrega generado automáticamente al confirmar la transacción. */
    @OneToOne(mappedBy = "orden", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DetalleOrdenEntrega detalleEntrega;
}
