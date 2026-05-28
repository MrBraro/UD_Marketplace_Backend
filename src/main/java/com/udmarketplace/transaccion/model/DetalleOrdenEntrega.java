package com.udmarketplace.transaccion.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa el detalle de una orden de entrega generada al confirmar
 * una transacción en el marketplace UD.
 *
 * <p>Mapea la tabla {@code detalle_orden_entrega}. Funciona como un <em>snapshot</em>
 * inmutable de los datos del producto al momento de la confirmación de compra:
 * nombre, descripción, precio unitario e imagen quedan fijados aunque el producto
 * sea modificado posteriormente por el vendedor.
 *
 * <p>La {@code confirmacionDigital} es un código único con formato
 * {@code CONF-{idOrden}-{UUID8}} que sirve como comprobante de la transacción.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Entity
@Table(name = "detalle_orden_entrega")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleOrdenEntrega {

    /** Identificador único auto-incremental del detalle de entrega. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    /** Orden de compra asociada (relación 1:1). Cada orden tiene exactamente un detalle. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", nullable = false, unique = true)
    private Orden orden;

    /** Nombre del producto capturado al momento de la confirmación . */
    @Column(name = "nombre_producto", length = 150)
    private String nombreProducto;

    /** Descripción del producto capturada al momento de la confirmación . */
    @Column(name = "descripcion_prod", length = 500)
    private String descripcionProd;

    /** Precio unitario del producto al momento de la transacción . */
    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    /** Imagen del producto capturada como BLOB al momento de la confirmación . */
    @Lob
    @Column(name = "imagen_producto")
    private byte[] imagenProducto;

    /** Fecha y hora en que se generó la orden de entrega. */
    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    /** Código de confirmación digital único en formato {@code CONF-{idOrden}-{UUID8}}. */
    @Column(name = "confirmacion_digital", unique = true, length = 255)
    private String confirmacionDigital;
}
