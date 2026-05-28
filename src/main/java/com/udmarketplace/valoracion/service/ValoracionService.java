/**
 * Contrato de negocio para la gestión de valoraciones y reputación en el marketplace UD.
 *
 * <p>Define las operaciones del ciclo de vida de una valoración:
 * <ul>
 *   <li>Registro con validación de compra previa y preservación del historial </li>
 *   <li>Cálculo de la calificación promedio de un producto </li>
 *   <li>Cálculo de la reputación del vendedor e inclusión de reseñas positivas</li>
 *   <li>Consulta del catálogo de reseñas predefinidas disponibles</li>
 *   <li>Inactivación lógica de valoraciones (historial conservado)</li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.valoracion.service;

import com.udmarketplace.valoracion.dto.CrearValoracionRequest;
import com.udmarketplace.valoracion.dto.ReputacionVendedorDto;
import com.udmarketplace.valoracion.dto.ValoracionDto;
import com.udmarketplace.valoracion.model.ResenaPredefinida;

import java.util.List;

public interface ValoracionService {

    /**
     * Registra una valoración de un comprador sobre un producto comprado.
     *
     * <p>Si el comprador ya tiene una valoración activa para el mismo producto, se inactiva
     * antes de crear la nueva (historial sin sobrescritura). Tras guardar la valoración
     * se actualiza automáticamente la reputación del vendedor.
     *
     * @param request         datos de la valoración (producto, orden, calificación, reseña opcional)
     * @param codigoComprador identificador del comprador autenticado
     * @return DTO de la valoración registrada
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException   si comprador, producto u orden no existen
     * @throws com.udmarketplace.auth.exception.OperacionNoPermitidaException  si la orden no pertenece al comprador,
     *                                                                          no cubre el producto o no está confirmada
     */
    ValoracionDto registrarValoracion(CrearValoracionRequest request, Long codigoComprador);

    /**
     * Calcula la calificación promedio de un producto a partir de sus valoraciones activas.
     *
     * @param idPub identificador del producto
     * @return promedio de calificaciones activas, o {@code null} si el producto no tiene valoraciones
     */
    Double calcularPromedioProducto(Long idPub);

    /**
     * Obtiene la reputación completa de un vendedor: promedio, total de reseñas positivas
     * (calificación ≥ 4) y total de valoraciones activas.
     *
     * @param codigoVendedor identificador del vendedor
     * @return DTO con el resumen de reputación del vendedor
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si el vendedor no existe
     */
    ReputacionVendedorDto obtenerReputacionVendedor(Long codigoVendedor);

    /**
     * Lista todas las valoraciones activas de un producto.
     *
     * @param idPub identificador del producto
     * @return lista de DTOs de valoraciones activas del producto
     */
    List<ValoracionDto> listarValoracionesProducto(Long idPub);

    /**
     * Retorna el catálogo de reseñas predefinidas activas disponibles para selección.
     *
     * @return lista de reseñas predefinidas con {@code activo = true}
     */
    List<ResenaPredefinida> listarResenasPredefinidas();

    /**
     * Marca una valoración como inactiva y recalcula la reputación del vendedor afectado.
     *
     * @param idVal identificador de la valoración a inactivar
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si la valoración no existe
     */
    void inactivarValoracion(Long idVal);
}
