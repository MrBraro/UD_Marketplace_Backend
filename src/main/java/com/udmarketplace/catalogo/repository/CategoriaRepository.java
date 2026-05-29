package com.udmarketplace.catalogo.repository;

import com.udmarketplace.catalogo.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Categoria}.
 * 
 *<p>Permite consultar, validar duplicados y filtrar categorías activas
 * para la gestión del catálogo.
 *
 * <p>Se usa en la capa de servicio para aplicar reglas de negocio como:
 * no duplicar nombres de categorías activas y listar solo categorías disponibles.
 *
 *
 * <p>Proporciona acceso a la tabla {@code categoria} en MySQL. Extiende
 * {@link JpaRepository} con métodos derivados para consultas frecuentes
 * del módulo de catálogo.
 *
 * @author Daniel Perez
 * @version 1.1
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

       /**
     * Verifica si ya existe una categoría activa con ese nombre,
     * ignorando diferencias entre mayúsculas y minúsculas.
     *
     * @param nombreCat nombre de la categoría
     * @return true si ya existe una categoría activa con ese nombre
     */
    boolean existsByNombreCatIgnoreCaseAndActivoCatTrue(String nombreCat);

    /**
     * Busca una categoría activa por su identificador.
     *
     * @param idCategoria identificador de la categoría
     * @return categoría activa si existe
     */
    Optional<Categoria> findByIdCategoriaAndActivoCatTrue(Long idCategoria);
}
