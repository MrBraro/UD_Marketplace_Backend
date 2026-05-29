package com.udmarketplace.catalogo.repository;

import com.udmarketplace.catalogo.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Categoria}.
 *
 * <p>Proporciona acceso a la tabla {@code categoria} en MySQL. Extiende
 * {@link JpaRepository} con métodos derivados para consultas frecuentes
 * del módulo de catálogo.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Retorna todas las categorías activas del catálogo ordenadas por ID.
     *
     * @return lista de categorías con {@code activoCat = true}
     */
    List<Categoria> findByActivoCatTrue();

    /**
     * Verifica si ya existe una categoría con el nombre indicado.
     * Útil para evitar duplicados al crear nuevas categorías.
     *
     * @param nombreCat nombre de la categoría a verificar
     * @return {@code true} si el nombre ya está registrado
     */
    boolean existsByNombreCat(String nombreCat);
}
