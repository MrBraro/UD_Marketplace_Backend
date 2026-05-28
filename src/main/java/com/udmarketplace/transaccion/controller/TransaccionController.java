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
    public ResponseEntity<TransaccionDto> confirmarTransaccion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoVendedor = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(transaccionService.confirmarTransaccion(id, codigoVendedor));
    }

    /**
     * Retorna el detalle completo de una transacción por su identificador.
     *
     * @param id identificador de la orden
     * @return DTO completo de la transacción
     */
    @GetMapping("/transacciones/{id}")
    public ResponseEntity<TransaccionDto> obtenerTransaccion(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerTransaccion(id));
    }

    /**
     * Consulta el historial de transacciones con filtros opcionales.
     * Los parámetros se pasan como query parameters y todos son opcionales.
     *
     * @param filtro parámetros de filtrado (comprador, vendedor, estado, rango de fechas)
     * @return lista de transacciones que coinciden con los filtros aplicados
     */
    @GetMapping("/transacciones")
    public ResponseEntity<List<TransaccionDto>> consultarHistorial(
            @ModelAttribute FiltroHistorialRequest filtro) {
        return ResponseEntity.ok(transaccionService.consultarHistorial(filtro));
    }
}
