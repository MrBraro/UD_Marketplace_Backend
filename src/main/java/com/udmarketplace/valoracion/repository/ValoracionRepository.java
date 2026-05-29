/**
 * Repositorio JPA para la entidad {@link com.udmarketplace.valoracion.model.Valoracion}.
 *
 * <p>Proporciona consultas de lectura filtradas por producto o vendedor,
 * verificación de duplicados , y cálculos JPQL para:
 * <ul>
 *   <li>Calificación promedio de un producto </li>
 *   <li>Reputación de un vendedor como promedio de sus valoraciones activas </li>
 *   <li>Conteo de reseñas positivas del vendedor calificación ≥ 4</li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.valoracion.repository;

import com.udmarketplace.valoracion.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    /**
     * Retorna todas las valoraciones activas de un producto, ordenadas por Spring Data.
     *
     * @param idPub identificador del producto
     * @return lista de valoraciones activas del producto
     */
    List<Valoracion> findByProducto_IdPubAndEstadoValoTrue(Long idPub);

    /**
     * Retorna todas las valoraciones activas recibidas por un vendedor.
     *
     * @param codigoVendedor identificador del vendedor
     * @return lista de valoraciones activas del vendedor
     */
    List<Valoracion> findByVendedor_CodigoUsuaAndEstadoValoTrue(Long codigoVendedor);

    /**
     * Verifica si un comprador ya tiene una valoración activa registrada para un producto.
     * Usado para impedir duplicados antes de crear una nueva valoración .
     *
     * @param codigoComprador identificador del comprador
     * @param idPub           identificador del producto
     * @return {@code true} si existe valoración activa previa; {@code false} en caso contrario
     */
    boolean existsByComprador_CodigoUsuaAndProducto_IdPubAndEstadoValoTrue(Long codigoComprador, Long idPub);

    /**
     * Calcula la calificación promedio de un producto a partir de sus valoraciones activas .
     *
     * @param idPub identificador del producto
     * @return promedio de calificaciones activas, o {@code null} si no hay valoraciones
     */
    @Query("SELECT AVG(v.calificacion) FROM Valoracion v WHERE v.producto.idPub = :idPub AND v.estadoValo = true")
    Double calcularPromedioProducto(@Param("idPub") Long idPub);

    /**
     * Calcula la reputación de un vendedor como el promedio de sus valoraciones activas .
     *
     * @param codigoVendedor identificador del vendedor
     * @return promedio de calificaciones activas del vendedor, o {@code null} si no hay valoraciones
     */
    @Query("SELECT AVG(v.calificacion) FROM Valoracion v WHERE v.vendedor.codigoUsua = :codigoVendedor AND v.estadoValo = true")
    Double calcularReputacionVendedor(@Param("codigoVendedor") Long codigoVendedor);

    /**
     * Cuenta las valoraciones activas con calificación ≥ 4 de un vendedor .
     *
     * @param codigoVendedor identificador del vendedor
     * @return cantidad de reseñas positivas activas del vendedor
     */
    @Query("SELECT COUNT(v) FROM Valoracion v WHERE v.vendedor.codigoUsua = :codigoVendedor AND v.estadoValo = true AND v.calificacion >= 4")
    long contarResenasPositivas(@Param("codigoVendedor") Long codigoVendedor);

    /**
     * Retorna todas las valoraciones de un comprador sobre un producto, sin importar su estado.
     * Utilizado para inactivar registros anteriores al actualizar una valoración .
     *
     * @param codigoComprador identificador del comprador
     * @param idPub           identificador del producto
     * @return lista de valoraciones del comprador para el producto (activas e inactivas)
     */
    List<Valoracion> findByComprador_CodigoUsuaAndProducto_IdPub(Long codigoComprador, Long idPub);
}
