/**
 * Controlador REST para la gestión de valoraciones y reputación del marketplace UD.
 *
 * <p>Expone los endpoints del módulo de valoraciones:
 * <ul>
 *   <li>{@code POST  /api/buyer/valoraciones}                     — registrar valoración (COMPRADOR)</li>
 *   <li>{@code GET   /api/valoraciones/producto/{id}}             — listar valoraciones del producto (público)</li>
 *   <li>{@code GET   /api/valoraciones/producto/{id}/promedio}    — calificación promedio del producto </li>
 *   <li>{@code GET   /api/valoraciones/vendedor/{id}/reputacion}  — reputación del vendedor </li>
 *   <li>{@code GET   /api/valoraciones/resenas}                   — catálogo de reseñas predefinidas</li>
 *   <li>{@code PATCH /api/admin/valoraciones/{id}/inactivar}      — inactivar valoración (ADMINISTRADOR)</li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.valoracion.controller;

import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.valoracion.dto.CrearValoracionRequest;
import com.udmarketplace.valoracion.dto.ReputacionVendedorDto;
import com.udmarketplace.valoracion.dto.ValoracionDto;
import com.udmarketplace.valoracion.model.ResenaPredefinida;
import com.udmarketplace.valoracion.service.ValoracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ValoracionController {

    /** Servicio de negocio para la gestión de valoraciones. */
    private final ValoracionService valoracionService;

    /** Utilidad JWT para extraer el ID del usuario del token de sesión. */
    private final JwtUtil jwtUtil;

    /**
     * Registra una valoración de un comprador sobre un producto comprado.
     * Si ya existe una valoración activa del mismo comprador para el producto, se inactiva
     * antes de crear la nueva (historial sin sobrescritura).
     *
     * @param request    datos de la valoración (producto, orden, calificación 1-5, reseña opcional)
     * @param authHeader header Authorization con el token Bearer del comprador autenticado
     * @return DTO de la valoración registrada con HTTP 201
     */
    @PostMapping("/buyer/valoraciones")
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<ValoracionDto> registrarValoracion(
            @Valid @RequestBody CrearValoracionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoComprador = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(valoracionService.registrarValoracion(request, codigoComprador));
    }

    /**
     * Lista todas las valoraciones activas de un producto.
     *
     * @param id identificador del producto
     * @return lista de DTOs de valoraciones activas del producto
     */
    @GetMapping("/valoraciones/producto/{id}")
    public ResponseEntity<List<ValoracionDto>> valoracionesProducto(@PathVariable Long id) {
        return ResponseEntity.ok(valoracionService.listarValoracionesProducto(id));
    }

    /**
     * Retorna la calificación promedio de un producto calculada desde sus valoraciones activas.
     *
     * @param id identificador del producto
     * @return promedio de calificaciones activas, o {@code null} si no hay valoraciones
     */
    @GetMapping("/valoraciones/producto/{id}/promedio")
    public ResponseEntity<Double> promedioProducto(@PathVariable Long id) {
        return ResponseEntity.ok(valoracionService.calcularPromedioProducto(id));
    }

    /**
     * Obtiene la reputación completa de un vendedor: promedio, reseñas positivas y
     * total de valoraciones activas.
     *
     * @param id identificador del vendedor
     * @return DTO de reputación del vendedor
     */
    @GetMapping("/valoraciones/vendedor/{id}/reputacion")
    public ResponseEntity<ReputacionVendedorDto> reputacionVendedor(@PathVariable Long id) {
        return ResponseEntity.ok(valoracionService.obtenerReputacionVendedor(id));
    }

    /**
     * Retorna el catálogo completo de reseñas predefinidas activas disponibles para selección.
     *
     * @return lista de reseñas predefinidas activas
     */
    @GetMapping("/valoraciones/resenas")
    public ResponseEntity<List<ResenaPredefinida>> listarResenas() {
        return ResponseEntity.ok(valoracionService.listarResenasPredefinidas());
    }

    /**
     * Marca una valoración como inactiva y recalcula la reputación del vendedor afectado.
     * Solo accesible para administradores.
     *
     * @param id identificador de la valoración a inactivar
     * @return respuesta vacía con HTTP 204
     */
    @PatchMapping("/admin/valoraciones/{id}/inactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inactivarValoracion(@PathVariable Long id) {
        valoracionService.inactivarValoracion(id);
        return ResponseEntity.noContent().build();
    }
}
