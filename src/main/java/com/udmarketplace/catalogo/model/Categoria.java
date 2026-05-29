package com.udmarketplace.catalogo.model;

import com.udmarketplace.auth.model.Administrador;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa una categoría del catálogo de productos del marketplace.
 *
 * <p>Mapea la tabla {@code categoria} de la base de datos MySQL. Cada categoría
 * agrupa productos relacionados y mantiene un contador de publicaciones activas
 * actualizado automáticamente a nivel de aplicación.
 *
 * <p>Solo los usuarios con rol {@code ADMINISTRADOR} pueden crear o inactivar categorías.
 * La eliminación es lógica: se establece {@code activoCat = false} sin borrar el registro.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Entity
@Table(name = "categoria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    /** Identificador único auto-incremental de la categoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    /** Nombre descriptivo de la categoría (máximo 100 caracteres, obligatorio). */
    @Column(name = "nombre_cat", nullable = false, length = 100)
    private String nombreCat;

    /** Indica si la categoría está activa. {@code false} representa eliminación lógica. */
    @Column(name = "activo_cat")
    private boolean activoCat = true;

    /** Descripción opcional de la categoría (máximo 500 caracteres). */
    @Column(name = "descripcion_cat", length = 500)
    private String descripcionCat;

    /** Contador de publicaciones activas asociadas a esta categoría. */
    @Column(name = "contador_productos")
    private int contadorProductos = 0;

    /** Administrador responsable de la creación o gestión de la categoría. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_admin")
    private Administrador administrador;
}
