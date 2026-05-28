package com.udmarketplace.transaccion.repository;

import com.udmarketplace.transaccion.model.DetalleOrdenEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link DetalleOrdenEntrega}.
 *
 * <p>Proporciona acceso a la tabla {@code detalle_orden_entrega} para consultar
 * el snapshot del producto generado al confirmar una transacción.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface DetalleOrdenEntregaRepository extends JpaRepository<DetalleOrdenEntrega, Long> {

    /**
     * Busca el detalle de entrega asociado a una orden específica.
     *
     * @param idOrden identificador de la orden de compra
     * @return el detalle de entrega si existe, vacío si la orden aún no fue confirmada
     */
    Optional<DetalleOrdenEntrega> findByOrden_IdOrden(Long idOrden);
}
