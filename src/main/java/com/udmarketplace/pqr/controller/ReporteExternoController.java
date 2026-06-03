package com.udmarketplace.pqr.controller;

import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.auth.service.PythonCouponClientService;
import com.udmarketplace.auth.service.PythonReportClientService;
import com.udmarketplace.transaccion.dto.ReporteExternoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para funcionalidades conectadas con los microservicios Python
 * de cupones y reportes externos.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Servicios Python", description = "Cupones de descuento y tracking de reportes externos")
@SecurityRequirement(name = "bearerAuth")
public class ReporteExternoController {

    private final PythonReportClientService pythonReportClient;
    private final PythonCouponClientService pythonCouponClient;
    private final JwtUtil jwtUtil;

    /**
     * Consulta el estado de un reporte externo por su radicado del report service Python.
     * El usuario puede usar este radicado (devuelto al crear la PQR) para hacer seguimiento.
     *
     * @param radicado número de radicado externo asignado por el report service Python
     * @return estado actual del reporte externo
     */
    @GetMapping("/reportes-externos/{radicado}")
    @Operation(
            summary = "Consultar estado de reporte externo",
            description = "Consulta el estado de una PQR en el sistema de reportes externo Python usando el radicado externo."
    )
    public ResponseEntity<ReporteExternoDto> consultarReporte(@PathVariable String radicado) {
        ReporteExternoDto dto = pythonReportClient.consultarPorRadicado(radicado);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * Lista los cupones disponibles del comprador autenticado.
     * Retorna el JSON crudo del coupon service Python.
     *
     * @param authHeader header Authorization con el token Bearer del comprador
     * @return JSON con cupones asignados y libres disponibles para el usuario
     */
    @GetMapping("/buyer/cupones")
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(
            summary = "Listar cupones del comprador",
            description = "Retorna los cupones disponibles para el comprador autenticado desde el coupon service Python."
    )
    public ResponseEntity<String> obtenerCupones(
            @RequestHeader("Authorization") String authHeader) {
        String correo = jwtUtil.extractCorreoUsuario(authHeader.substring(7));
        String cupones = pythonCouponClient.obtenerCuponesUsuario(correo);
        if (cupones == null) {
            return ResponseEntity.ok("{}");
        }
        return ResponseEntity.ok(cupones);
    }
}
