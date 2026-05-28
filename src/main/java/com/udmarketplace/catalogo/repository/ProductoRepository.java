package com.udmarketplace.catalogo.repository;

import com.udmarketplace.catalogo.model.Producto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Producto}.
 *
 * <p>Extiende {@link JpaRepository} para operaciones CRUD estándar y
 * {@link JpaSpecificationExecutor} para la búsqueda dinámica con filtros (REQ catálogo).
 *
 * <p>Incluye consultas JPQL para calcular el promedio de calificaciones de un producto
 * a partir de sus valoraciones activas (REQ-15).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    /**
     * Retorna los productos activos de un vendedor específico ordenados según el criterio indicado.
     *
     * @param codigoVendedor identificador del vendedor propietario de los productos
     * @param sort           criterio de ordenamiento (precio, fecha, nombre)
     * @return lista de productos activos del vendedor
     */
    List<Producto> findByVendedor_CodigoUsuaAndActivoPubTrue(Long codigoVendedor, Sort sort);

    /**
     * Retorna todos los productos activos del catálogo con el ordenamiento indicado.
     *
     * @param sort criterio de ordenamiento aplicado al resultado
     * @return lista de todos los productos con {@code activoPub = true}
     */
    List<Producto> findByActivoPubTrue(Sort sort);

    /**
     * Calcula la calificación promedio de un producto a partir de sus valoraciones activas (REQ-15).
     *
     * @param idPub identificador del producto
     * @return promedio de calificaciones activas, o {@code null} si no tiene valoraciones
     */
    @Query("SELECT AVG(v.calificacion) FROM Valoracion v " +
           "WHERE v.producto.idPub = :idPub AND v.estadoValo = true")
    Double calcularCalificacionPromedio(@Param("idPub") Long idPub);
}
