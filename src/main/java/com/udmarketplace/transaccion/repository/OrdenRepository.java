package com.udmarketplace.transaccion.repository;

import com.udmarketplace.transaccion.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Orden}.
 *
 * <p>Proporciona acceso a la tabla {@code orden} con consultas para el historial
 * filtrable de transacciones  y la confirmación por vendedor.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    /**
     * Retorna todas las órdenes donde el comprador identificado por su código sea el de la orden.
     *
     * @param codigoComprador identificador del comprador
     * @return lista de órdenes del comprador
     */
    List<Orden> findByComprador_CodigoUsua(Long codigoComprador);

    /**
     * Retorna todas las órdenes donde el vendedor identificado por su código sea el de la orden.
     *
     * @param codigoVendedor identificador del vendedor
     * @return lista de órdenes del vendedor
     */
    List<Orden> findByVendedor_CodigoUsua(Long codigoVendedor);

    /**
     * Consulta el historial de transacciones aplicando filtros opcionales.
     * Cada parámetro {@code null} se ignora en la consulta.
     *
     * @param codigoComprador filtro por identificador del comprador (nullable)
     * @param codigoVendedor  filtro por identificador del vendedor (nullable)
     * @param estado          filtro por estado de la orden (nullable)
     * @param desde           fecha/hora de inicio del rango (nullable)
     * @param hasta           fecha/hora de fin del rango (nullable)
     * @return lista de órdenes que cumplen con todos los filtros aplicados
     */
    @Query("SELECT o FROM Orden o WHERE " +
           "(:codigoComprador IS NULL OR o.comprador.codigoUsua = :codigoComprador) AND " +
           "(:codigoVendedor  IS NULL OR o.vendedor.codigoUsua  = :codigoVendedor)  AND " +
           "(:estado          IS NULL OR o.estadoOrden          = :estado)          AND " +
           "(:desde           IS NULL OR o.datetimeCompra       >= :desde)          AND " +
           "(:hasta           IS NULL OR o.datetimeCompra       <= :hasta)")
    List<Orden> buscarHistorial(@Param("codigoComprador") Long codigoComprador,
                                @Param("codigoVendedor")  Long codigoVendedor,
                                @Param("estado")          String estado,
                                @Param("desde")           LocalDateTime desde,
                                @Param("hasta")           LocalDateTime hasta);

    /**
     * Busca una orden específica verificando que pertenezca al vendedor indicado.
     * Usado en el flujo de confirmación (REQ-06) para evitar que un vendedor confirme
     * transacciones de otro.
     *
     * @param idOrden        identificador de la orden
     * @param codigoVendedor identificador del vendedor propietario
     * @return la orden si existe y pertenece al vendedor; vacío en caso contrario
     */
    Optional<Orden> findByIdOrdenAndVendedor_CodigoUsua(Long idOrden, Long codigoVendedor);
}
