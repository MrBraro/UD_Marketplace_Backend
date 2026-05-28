package com.udmarketplace.catalogo.service;

import com.udmarketplace.catalogo.dto.CategoriaDto;
import com.udmarketplace.catalogo.dto.CrearCategoriaRequest;

import java.util.List;

/**
 * Contrato del servicio de gestión de categorías del catálogo UD Marketplace.
 *
 * <p>Administra el ciclo de vida de las categorías del sistema: creación, consulta,
 * inactivación lógica y mantenimiento del contador de productos activos.
 *
 * <p>Restricciones de acceso:
 * <ul>
 *   <li>Crear e inactivar categorías: solo usuarios con rol {@code ADMINISTRADOR}</li>
 *   <li>Consultar categorías activas: público (sin autenticación)</li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface CategoriaService {

    /**
     * Crea una nueva categoría en el catálogo, asociándola al administrador que la registra.
     *
     * @param request    datos de la nueva categoría (nombre y descripción)
     * @param codigoAdmin identificador del administrador que crea la categoría
     * @return DTO con los datos de la categoría creada
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si el administrador no existe
     */
    CategoriaDto crearCategoria(CrearCategoriaRequest request, Long codigoAdmin);

    /**
     * Retorna todas las categorías activas del catálogo.
     *
     * @return lista de categorías con {@code activoCat = true}
     */
    List<CategoriaDto> listarCategoriasActivas();

    /**
     * Retorna el detalle de una categoría por su identificador.
     *
     * @param idCategoria identificador de la categoría
     * @return DTO con los datos de la categoría
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si la categoría no existe
     */
    CategoriaDto obtenerCategoria(Long idCategoria);

    /**
     * Marca una categoría como inactiva (eliminación lógica). Solo accesible para administradores.
     *
     * @param idCategoria identificador de la categoría a inactivar
     * @param codigoAdmin identificador del administrador que realiza la acción
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si la categoría no existe
     */
    void inactivarCategoria(Long idCategoria, Long codigoAdmin);

    /**
     * Incrementa en uno el contador de productos activos de la categoría (REQ-04).
     * Se invoca automáticamente cuando se registra un nuevo producto en la categoría.
     *
     * @param idCategoria identificador de la categoría cuyo contador se incrementa
     */
    void incrementarContador(Long idCategoria);

    /**
     * Decrementa en uno el contador de productos activos de la categoría (REQ-04).
     * Se invoca automáticamente cuando un producto se inactiva o cambia de categoría.
     * El valor mínimo del contador es 0.
     *
     * @param idCategoria identificador de la categoría cuyo contador se decrementa
     */
    void decrementarContador(Long idCategoria);
}
