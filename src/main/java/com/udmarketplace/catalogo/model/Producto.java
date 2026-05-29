package com.udmarketplace.catalogo.model;

import com.udmarketplace.auth.model.Vendedor;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una publicación de producto en el marketplace UD.
 *
 * <p>Mapea la tabla {@code producto} de la base de datos MySQL. Cada producto
 * pertenece a un vendedor y a una categoría activa del catálogo.
 *
 * <p>La eliminación es lógica mediante {@code activoPub = false}, lo que actualiza
 * automáticamente el contador de la categoría asociada .
 *
 * <p>La imagen se almacena como BLOB en la base de datos. Se valida su contenido
 * en la capa de servicio antes de persistir (tipo MIME, extensión y tamaño máximo).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Entity
@Table(name = "producto")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    /** Identificador único auto-incremental de la publicación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pub")
    private Long idPub;

    /** Vendedor propietario del producto. Relación Many-to-One con la tabla {@code usuario}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario", nullable = false)
    private Vendedor vendedor;

    /** Nombre del producto visible en el catálogo (máximo 150 caracteres). */
    @Column(name = "nombre_pub", length = 150)
    private String nombrePub;

    /** Descripción detallada del producto (máximo 500 caracteres). */
    @Column(name = "descripcion_pub", length = 500)
    private String descripcionPub;

    /** Imagen del producto almacenada como BLOB en la base de datos. */
    @Lob
    @Column(name = "imagen_pub")
    private byte[] imagenPub;

    /** Ubicación geográfica del vendedor o del producto (máximo 200 caracteres). */
    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    /** Precio unitario del producto con precisión de 12 dígitos y 2 decimales. */
    @Column(name = "precio_pub", precision = 12, scale = 2)
    private BigDecimal precioPub;

    /** Indica si el producto está disponible para ser comprado. */
    @Column(name = "disponibilidad")
    private boolean disponibilidad = true;

    /** Categoría del catálogo a la que pertenece el producto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    /** Condiciones de venta definidas por el vendedor (máximo 500 caracteres). */
    @Column(name = "condiciones_venta", length = 500)
    private String condicionesVenta;

    /** Fecha y hora de registro del producto en el sistema. */
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    /** Indica si el producto está activo. {@code false} representa eliminación lógica */
    @Column(name = "activo_pub")
    private boolean activoPub = true;
}
