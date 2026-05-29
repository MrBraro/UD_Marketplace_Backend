package com.udmarketplace.pqr.controller;

import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.pqr.dto.AgregarInteraccionRequest;
import com.udmarketplace.pqr.dto.CrearPqrRequest;
import com.udmarketplace.pqr.dto.InteraccionDto;
import com.udmarketplace.pqr.dto.PqrDto;
import com.udmarketplace.pqr.service.PqrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para la gestión de PQRs (Peticiones, Quejas y Reclamos) del marketplace UD.
 *
 * <p>Expone los endpoints del ciclo de vida de las PQRs:
 * <ul>
 *   <li>{@code POST  /api/pqrs}                          — crear PQR con adjunto (autenticado)</li>
 *   <li>{@code GET   /api/pqrs}                          — listar PQRs del usuario (autenticado)</li>
 *   <li>{@code GET   /api/pqrs/{radicado}}               — detalle de PQR (creador o admin)</li>
 *   <li>{@code POST  /api/pqrs/{radicado}/interacciones} — agregar mensaje (autenticado)</li>
 *   <li>{@code PATCH /api/admin/pqrs/{radicado}/estado}  — cambiar estado (ADMINISTRADOR)</li>
 *   <li>{@code PATCH /api/admin/pqrs/{radicado}/cerrar}  — cerrar PQR (ADMINISTRADOR)</li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PqrController {

    /** Servicio de negocio para la gestión de PQRs. */
    private final PqrService pqrService;

    /** Utilidad JWT para extraer el ID del usuario del token de sesión. */
    private final JwtUtil jwtUtil;

    /**
     * Crea una nueva PQR. El radicado, fecha/hora y administrador asignado
     * se establecen automáticamente (REQ-10, REQ-11, REQ-13).
     *
     * @param request    datos de la PQR (tipo y descripción)
     * @param adjunto    archivo adjunto opcional (imagen/PDF, máx. 5 MB, REQ-12)
     * @param authHeader header Authorization con el token Bearer del usuario
     * @return DTO de la PQR creada con HTTP 201
     */
    @PostMapping("/pqrs")
    public ResponseEntity<PqrDto> crearPqr(
            @Valid @RequestPart("datos") CrearPqrRequest request,
            @RequestPart(value = "adjunto", required = false) MultipartFile adjunto,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoUsuario = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pqrService.crearPqr(request, adjunto, codigoUsuario));
    }

    /**
     * Lista todas las PQRs creadas por el usuario autenticado.
     *
     * @param authHeader header Authorization con el token Bearer del usuario
     * @return lista de PQRs del usuario
     */
    @GetMapping("/pqrs")
    public ResponseEntity<List<PqrDto>> listarPqrs(
            @RequestHeader("Authorization") String authHeader) {
        Long codigoUsuario = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(pqrService.listarPqrsUsuario(codigoUsuario));
    }

    /**
     * Retorna el detalle de una PQR. Solo accesible para el usuario creador o el administrador asignado.
     *
     * @param radicado   número de radicado de la PQR
     * @param authHeader header Authorization con el token Bearer del solicitante
     * @return DTO completo de la PQR con su historial de interacciones
     */
    @GetMapping("/pqrs/{radicado}")
    public ResponseEntity<PqrDto> obtenerPqr(
            @PathVariable Long radicado,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoUsuario = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(pqrService.obtenerPqr(radicado, codigoUsuario));
    }

    /**
     * Agrega un mensaje/interacción a una PQR existente (REQ-14).
     * No se permiten interacciones en PQRs cerradas.
     *
     * @param radicado   número de radicado de la PQR
     * @param request    DTO con el mensaje de la interacción
     * @param authHeader header Authorization con el token Bearer del autor del mensaje
     * @return DTO de la interacción registrada con HTTP 201
     */
    @PostMapping("/pqrs/{radicado}/interacciones")
    public ResponseEntity<InteraccionDto> agregarInteraccion(
            @PathVariable Long radicado,
            @Valid @RequestBody AgregarInteraccionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoAutor = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pqrService.agregarInteraccion(radicado, request, codigoAutor));
    }

    /**
     * Cambia el estado de una PQR. Solo accesible para administradores.
     * Estados válidos: ENVIADA, EN_PROCESO, CERRADA.
     *
     * @param radicado   número de radicado de la PQR
     * @param estado     nuevo estado (query parameter)
     * @param authHeader header Authorization con el token Bearer del administrador
     * @return DTO de la PQR con el estado actualizado
     */
    @PatchMapping("/admin/pqrs/{radicado}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PqrDto> cambiarEstado(
            @PathVariable Long radicado,
            @RequestParam String estado,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoAdmin = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(pqrService.cambiarEstado(radicado, estado, codigoAdmin));
    }

    /**
     * Cierra definitivamente una PQR. Solo accesible para administradores.
     * Una PQR cerrada no acepta nuevas interacciones.
     *
     * @param radicado   número de radicado de la PQR a cerrar
     * @param authHeader header Authorization con el token Bearer del administrador
     * @return DTO de la PQR con estado CERRADA
     */
    @PatchMapping("/admin/pqrs/{radicado}/cerrar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PqrDto> cerrarPqr(
            @PathVariable Long radicado,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoAdmin = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(pqrService.cerrarPqr(radicado, codigoAdmin));
    }
}
