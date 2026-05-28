package com.udmarketplace.catalogo.controller;

import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.catalogo.dto.CategoriaDto;
import com.udmarketplace.catalogo.dto.CrearCategoriaRequest;
import com.udmarketplace.catalogo.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de categorías del catálogo UD Marketplace.
 *
 * <p>Expone los endpoints de administración y consulta de categorías:
 * <ul>
 *   <li>{@code POST   /api/admin/categorias}            — crear categoría (solo ADMINISTRADOR)</li>
 *   <li>{@code GET    /api/categorias}                  — listar categorías activas (público)</li>
 *   <li>{@code GET    /api/categorias/{id}}             — detalle de categoría (público)</li>
 *   <li>{@code PATCH  /api/admin/categorias/{id}/inactivar} — inactivar categoría (solo ADMINISTRADOR)</li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@RestController
@RequiredArgsConstructor
public class CategoriaController {

    /** Servicio de negocio para la gestión de categorías. */
    private final CategoriaService categoriaService;

    /** Utilidad JWT para extraer el ID del administrador del token. */
    private final JwtUtil jwtUtil;

    /**
     * Crea una nueva categoría en el catálogo. Solo accesible para administradores.
     *
     * @param request    datos de la nueva categoría
     * @param authHeader header Authorization con el token Bearer del administrador
     * @return DTO de la categoría creada con HTTP 201
     */
    @PostMapping("/api/admin/categorias")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CategoriaDto> crearCategoria(
            @Valid @RequestBody CrearCategoriaRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoAdmin = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoriaService.crearCategoria(request, codigoAdmin));
    }

    /**
     * Lista todas las categorías activas del catálogo. Endpoint público.
     *
     * @return lista de categorías activas
     */
    @GetMapping("/api/categorias")
    public ResponseEntity<List<CategoriaDto>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategoriasActivas());
    }

    /**
     * Retorna el detalle de una categoría por su identificador. Endpoint público.
     *
     * @param id identificador de la categoría
     * @return DTO con los datos de la categoría
     */
    @GetMapping("/api/categorias/{id}")
    public ResponseEntity<CategoriaDto> obtenerCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerCategoria(id));
    }

    /**
     * Marca una categoría como inactiva (eliminación lógica). Solo accesible para administradores.
     *
     * @param id         identificador de la categoría a inactivar
     * @param authHeader header Authorization con el token Bearer del administrador
     * @return HTTP 204 sin contenido
     */
    @PatchMapping("/api/admin/categorias/{id}/inactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inactivarCategoria(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoAdmin = jwtUtil.extractUserId(authHeader.substring(7));
        categoriaService.inactivarCategoria(id, codigoAdmin);
        return ResponseEntity.noContent().build();
    }
}
