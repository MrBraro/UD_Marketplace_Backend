/**
 * Repositorio JPA para la entidad {@link com.udmarketplace.valoracion.model.ResenaPredefinida}.
 *
 * <p>Gestiona el catálogo de reseñas predefinidas disponibles para que los
 * compradores las asocien a sus valoraciones .
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.valoracion.repository;

import com.udmarketplace.valoracion.model.ResenaPredefinida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResenaPredefinidaRepository extends JpaRepository<ResenaPredefinida, Long> {

    /**
     * Retorna únicamente las reseñas predefinidas activas, disponibles para selección.
     *
     * @return lista de reseñas con {@code activo = true}
     */
    List<ResenaPredefinida> findByActivoTrue();
}
