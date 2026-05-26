package com.udmarketplace.auth.controller;

import com.udmarketplace.auth.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de recursos protegidos por rol (RF24).
 *
 * <p>Demuestra la autorización basada en roles con endpoints stub.
 * Estos endpoints serán reemplazados por los equipos responsables de
 * cada dominio funcional (administración, vendedores, compradores).
 *
 * <p>La restricción de acceso se aplica en dos capas:
 * <ol>
 *   <li>A nivel de URL en {@code SecurityConfig} (restricción por path)</li>
 *   <li>A nivel de método con {@code @PreAuthorize} (restricción explícita)</li>
 * </ol>
 *
 * <p>Todos los endpoints requieren header: {@code Authorization: Bearer <token>}
 */
@RestController
@RequiredArgsConstructor
public class ProtectedController {

    /**
     * GET /api/admin/dashboard
     *
     * <p>Acceso exclusivo para rol ADMIN.
     * Retorna 403 si el usuario autenticado tiene rol SELLER o BUYER.
     */
    @GetMapping("/api/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> adminDashboard() {
        return ResponseEntity.ok(
                new MessageResponse("Bienvenido al panel administrativo — Acceso ADMIN")
        );
    }

    /**
     * GET /api/seller/products
     *
     * <p>Acceso exclusivo para rol SELLER.
     * Retorna 403 si el usuario autenticado tiene rol ADMIN o BUYER.
     */
    @GetMapping("/api/seller/products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<MessageResponse> sellerProducts() {
        return ResponseEntity.ok(
                new MessageResponse("Gestión de productos del vendedor — Acceso SELLER")
        );
    }

    /**
     * GET /api/buyer/catalog
     *
     * <p>Acceso exclusivo para rol BUYER.
     * Retorna 403 si el usuario autenticado tiene rol ADMIN o SELLER.
     */
    @GetMapping("/api/buyer/catalog")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<MessageResponse> buyerCatalog() {
        return ResponseEntity.ok(
                new MessageResponse("Catálogo de productos — Acceso BUYER")
        );
    }
}
