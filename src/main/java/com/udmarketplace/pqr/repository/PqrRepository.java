package com.udmarketplace.pqr.repository;

import com.udmarketplace.pqr.model.Pqr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Pqr}.
 *
 * <p>Proporciona acceso a la tabla {@code pqr} con consultas para la asignación
 * automática de administradores por carga (REQ-13) y el listado por usuario.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface PqrRepository extends JpaRepository<Pqr, Long> {

    /**
     * Retorna todas las PQRs creadas por el usuario identificado.
     *
     * @param codigoUsuario identificador del usuario creador
     * @return lista de PQRs del usuario
     */
    List<Pqr> findByUsuario_CodigoUsua(Long codigoUsuario);

    /**
     * Retorna el conteo de PQRs abiertas (no cerradas) agrupado por administrador,
     * ordenado ascendentemente para identificar al administrador con menor carga (REQ-13).
     *
     * @return lista de arreglos {@code [codigoAdmin, totalPqrsAbiertas]} ordenada por carga ascendente
     */
    @Query("SELECT p.administrador.codigoUsua, COUNT(p) as total " +
           "FROM Pqr p " +
           "WHERE p.administrador IS NOT NULL AND p.estadoPqr != 'CERRADA' " +
           "GROUP BY p.administrador.codigoUsua " +
           "ORDER BY total ASC")
    List<Object[]> contarPqrsAbiertas();

    /**
     * Busca una PQR verificando que pertenezca al usuario indicado.
     *
     * @param radicado      número de radicado de la PQR
     * @param codigoUsuario identificador del usuario creador
     * @return la PQR si existe y pertenece al usuario; vacío en caso contrario
     */
    Optional<Pqr> findByRadicadoAndUsuario_CodigoUsua(Long radicado, Long codigoUsuario);
}
