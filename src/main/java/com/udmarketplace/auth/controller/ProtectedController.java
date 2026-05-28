/**
 * Controlador de recursos de prueba protegidos por rol en el marketplace UD.
 *
 * <p>Expone tres endpoints de verificación del sistema de autorización RBAC,
 * uno por cada rol disponible (ADMINISTRADOR, VENDEDOR, COMPRADOR). Sirve para
 * confirmar que el filtro JWT y las anotaciones {@code @PreAuthorize} funcionan
 * correctamente. No expone lógica de negocio real.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.controller;

import com.udmarketplace.auth.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProtectedController {

    /**
     * GET /api/admin/dashboard
     *
     * <p>Acceso exclusivo para rol ADMINISTRADOR.
     */
    @GetMapping("/api/admin/dashboard")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MessageResponse> adminDashboard() {
        return ResponseEntity.ok(
                new MessageResponse("Bienvenido al panel administrativo — Acceso ADMINISTRADOR")
        );
    }

    /**
     * GET /api/seller/products
     *
     * <p>Acceso exclusivo para rol VENDEDOR.
     */
    @GetMapping("/api/seller/products")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<MessageResponse> sellerProducts() {
        return ResponseEntity.ok(
                new MessageResponse("Gestión de productos del vendedor — Acceso VENDEDOR")
        );
    }

    /**
     * GET /api/buyer/catalog
     *
     * <p>Acceso exclusivo para rol COMPRADOR.
     */
    @GetMapping("/api/buyer/catalog")
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<MessageResponse> buyerCatalog() {
        return ResponseEntity.ok(
                new MessageResponse("Catálogo de productos — Acceso COMPRADOR")
        );
    }
}
