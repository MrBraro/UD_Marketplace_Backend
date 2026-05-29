package com.udmarketplace.pqr.repository;

import com.udmarketplace.pqr.model.InteraccionPqr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link InteraccionPqr}.
 *
 * <p>Proporciona acceso a la tabla {@code interaccion_pqr} para consultar
 * el historial cronológico de mensajes de una PQR (REQ-14).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface InteraccionPqrRepository extends JpaRepository<InteraccionPqr, Long> {

    /**
     * Retorna todas las interacciones de una PQR ordenadas cronológicamente ascendente.
     *
     * @param radicado número de radicado de la PQR
     * @return lista de interacciones ordenadas por fecha y hora de más antigua a más reciente
     */
    List<InteraccionPqr> findByPqr_RadicadoOrderByFechaHoraAsc(Long radicado);
}
