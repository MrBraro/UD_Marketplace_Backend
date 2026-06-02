package com.udmarketplace.transaccion.controller;

import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.transaccion.dto.CrearTransaccionRequest;
import com.udmarketplace.transaccion.dto.FiltroHistorialRequest;
import com.udmarketplace.transaccion.dto.TransaccionDto;
import com.udmarketplace.transaccion.service.TransaccionService;
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
 * Controlador REST para la gestión de transacciones del marketplace UD.
 *
 * <p>Expone los endpoints del flujo de compra-venta:
 * <ul>
 *   <li>{@code POST /api/buyer/transacciones}              — registrar intención de compra </li>
 *   <li>{@code POST /api/seller/transacciones/{id}/confirmar} — confirmar transacción (VENDEDOR)</li>
 *   <li>{@code GET  /api/transacciones/{id}}               — detalle de transacción (autenticado)</li>
 *   <li>{@code GET  /api/transacciones}                    — historial con filtros (autenticado)</li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Endpoints para el registro de intenciones de compra, confirmación de pedidos e historial de transacciones")
@SecurityRequirement(name = "bearerAuth")
public class TransaccionController {

    /** Servicio de negocio para la gestión de transacciones. */
    private final TransaccionService transaccionService;

    /** Utilidad JWT para extraer el ID del usuario del token de sesión. */
    private final JwtUtil jwtUtil;

    /**
     * Registra la intención de compra de un producto .
     * Solo accesible para usuarios con rol {@code COMPRADOR}.
     *
     * @param request    DTO con el identificador del producto a comprar
     * @param authHeader header Authorization con el token Bearer del comprador
     * @return DTO de la orden creada en estado PENDIENTE con HTTP 201
     */
    @PostMapping("/buyer/transacciones")
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(
            summary = "Registrar intención de compra",
            description = "Permite a un comprador iniciar una transacción para adquirir un producto. La orden se crea en estado PENDIENTE. Requiere rol COMPRADOR.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Intención de compra registrada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "El producto no está disponible o datos inválidos"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - Requiere rol COMPRADOR")
            }
    )
    public ResponseEntity<TransaccionDto> registrarCompra(
            @Valid @RequestBody CrearTransaccionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoComprador = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionService.registrarIntencioneCompra(request, codigoComprador));
    }

    /**
     * Confirma una transacción pendiente. Solo el vendedor propietario puede confirmarla.
     * Al confirmar se genera automáticamente la orden de entrega con snapshot del producto.
     *
     * @param id         identificador de la orden a confirmar
     * @param authHeader header Authorization con el token Bearer del vendedor
     * @return DTO de la transacción confirmada con el detalle de entrega
     */
    @PostMapping("/seller/transacciones/{id}/confirmar")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(
            summary = "Confirmar transacción (Vendedor)",
            description = "Permite al vendedor del producto confirmar la venta. Al hacerlo, la transacción pasa a estado CONFIRMADA y se genera el detalle de entrega digital. Registra evento en auditoría. Requiere rol VENDEDOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transacción confirmada exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - No es el vendedor propietario del producto o permisos insuficientes"),
                    @ApiResponse(responseCode = "404", description = "Transacción no encontrada")
            }
    )
    public ResponseEntity<TransaccionDto> confirmarTransaccion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoVendedor = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(transaccionService.confirmarTransaccion(id, codigoVendedor));
    }

    /**
     * Retorna el detalle completo de una transacción por su identificador.
     * Solo el comprador o el vendedor involucrados pueden acceder a la misma.
     *
     * @param id         identificador de la orden
     * @param authHeader header Authorization con el token Bearer del usuario
     * @return DTO completo de la transacción
     */
    @GetMapping("/transacciones/{id}")
    @Operation(
            summary = "Obtener detalle de transacción",
            description = "Consulta la información completa de una transacción por su ID. Solo el comprador o vendedor involucrado puede acceder.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Detalle de transacción obtenido exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - No es el comprador ni el vendedor de esta transacción"),
                    @ApiResponse(responseCode = "404", description = "Transacción no encontrada")
            }
    )
    public ResponseEntity<TransaccionDto> obtenerTransaccion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader.substring(7));
        TransaccionDto dto = transaccionService.obtenerTransaccion(id);
        boolean esComprador = userId.equals(dto.getIdComprador());
        boolean esVendedor = dto.getIdVendedor() != null && userId.equals(dto.getIdVendedor());
        if (!esComprador && !esVendedor) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Consulta el historial de transacciones con filtros opcionales.
     * Compradores solo ven sus propias compras; vendedores sus propias ventas;
     * administradores pueden filtrar libremente.
     *
     * @param filtro     parámetros de filtrado (estado, rango de fechas)
     * @param authHeader header Authorization con el token Bearer del usuario
     * @return lista de transacciones que coinciden con los filtros aplicados
     */
    @GetMapping("/transacciones")
    @Operation(
            summary = "Consultar historial de transacciones",
            description = "Retorna transacciones del usuario autenticado. Compradores ven sus compras, vendedores sus ventas, administradores pueden filtrar por cualquier usuario.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Historial consultado exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado")
            }
    )
    public ResponseEntity<List<TransaccionDto>> consultarHistorial(
            @ModelAttribute FiltroHistorialRequest filtro,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader.substring(7));
        String role = jwtUtil.extractRole(authHeader.substring(7));
        if ("COMPRADOR".equals(role)) {
            filtro.setCodigoComprador(userId);
        } else if ("VENDEDOR".equals(role)) {
            filtro.setCodigoVendedor(userId);
        }
        // ADMINISTRADOR puede filtrar libremente
        return ResponseEntity.ok(transaccionService.consultarHistorial(filtro));
    }
}
