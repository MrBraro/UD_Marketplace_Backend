package com.udmarketplace.auth.controller;

import com.udmarketplace.auth.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de recursos protegidos por rol (RF24) alineados al ER.
 */
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
