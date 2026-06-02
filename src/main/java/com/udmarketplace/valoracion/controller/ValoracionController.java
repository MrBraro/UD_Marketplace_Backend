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

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

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
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Valoraciones", description = "Endpoints para el registro de calificaciones, reseñas y reputación de vendedores")
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
    @Operation(
            summary = "Registrar valoración sobre un producto",
            description = "Permite a un comprador calificar y escribir una reseña sobre un producto comprado en una transacción finalizada. Inactiva valoraciones previas para mantener historial. Requiere rol COMPRADOR.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Valoración registrada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o la transacción no pertenece al comprador o no está finalizada"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - Requiere rol COMPRADOR")
            }
    )
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
    @Operation(
            summary = "Listar valoraciones de un producto",
            description = "Retorna una lista de todas las valoraciones activas (no eliminadas ni inactivadas) asociadas a un producto. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de valoraciones obtenida exitosamente")
            }
    )
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
    @Operation(
            summary = "Obtener calificación promedio de un producto",
            description = "Retorna el valor promedio de las calificaciones activas de un producto. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Calificación promedio obtenida exitosamente")
            }
    )
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
    @Operation(
            summary = "Obtener reputación del vendedor",
            description = "Retorna la puntuación promedio general del vendedor y estadísticas sobre sus valoraciones recibidas. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reputación del vendedor obtenida exitosamente")
            }
    )
    public ResponseEntity<ReputacionVendedorDto> reputacionVendedor(@PathVariable Long id) {
        return ResponseEntity.ok(valoracionService.obtenerReputacionVendedor(id));
    }

    /**
     * Retorna el catálogo completo de reseñas predefinidas activas disponibles para selección.
     *
     * @return lista de reseñas predefinidas activas
     */
    @GetMapping("/valoraciones/resenas")
    @Operation(
            summary = "Listar reseñas predefinidas",
            description = "Retorna una lista de las reseñas textuales predefinidas que los compradores pueden elegir rápidamente. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Catálogo de reseñas obtenido exitosamente")
            }
    )
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
    @Operation(
            summary = "Inactivar valoración (Admin)",
            description = "Permite a un administrador inactivar (moderación/eliminación lógica) una valoración. Recalcula automáticamente la reputación del vendedor. Requiere rol ADMINISTRADOR.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Valoración inactivada exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - Requiere rol ADMINISTRADOR"),
                    @ApiResponse(responseCode = "404", description = "Valoración no encontrada")
            }
    )
    public ResponseEntity<Void> inactivarValoracion(@PathVariable Long id) {
        valoracionService.inactivarValoracion(id);
        return ResponseEntity.noContent().build();
    }
}
