package com.udmarketplace.catalogo.controller;

import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.catalogo.dto.CrearProductoRequest;
import com.udmarketplace.catalogo.dto.FiltroProductoRequest;
import com.udmarketplace.catalogo.dto.ProductoDto;
import com.udmarketplace.catalogo.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para la gestión de publicaciones del catálogo UD Marketplace.
 *
 * <p>Expone los endpoints del ciclo de vida de los productos:
 * <ul>
 *   <li>{@code POST   /api/seller/productos}     — registrar producto (VENDEDOR)</li>
 *   <li>{@code GET    /api/productos}             — buscar con filtros (público)</li>
 *   <li>{@code GET    /api/productos/{id}}        — detalle completo (público)</li>
 *   <li>{@code GET    /api/seller/productos}      — mis productos del vendedor (VENDEDOR)</li>
 *   <li>{@code PUT    /api/seller/productos/{id}} — actualizar producto (VENDEDOR)</li>
 *   <li>{@code DELETE /api/seller/productos/{id}} — eliminación lógica (VENDEDOR)</li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.1
 * @since 2026-05-28
 */
@RestController
@RequiredArgsConstructor
public class ProductoController {

    /** Servicio de negocio para la gestión de productos. */
    private final ProductoService productoService;

    /** Utilidad JWT para extraer el ID del vendedor del token de sesión. */
    private final JwtUtil jwtUtil;

    /**
     * Registra un nuevo producto en el catálogo asociado al vendedor autenticado.
     * Actualiza el contador de la categoría (REQ-04). Requiere {@code multipart/form-data}.
     *
     * @param request    datos del producto en la parte {@code datos} del multipart
     * @param imagen     imagen opcional del producto (parte {@code imagen} del multipart)
     * @param authHeader header Authorization con el token Bearer del vendedor
     * @return DTO del producto creado con HTTP 201
     */
    @PostMapping(value = "/api/seller/productos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoDto> registrarProducto(
            @Valid @RequestPart("datos") CrearProductoRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoVendedor = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoService.registrarProducto(request, imagen, codigoVendedor));
    }

    /**
     * Busca productos aplicando los filtros recibidos como query parameters.
     * Endpoint público, no requiere autenticación.
     *
     * @param filtro parámetros de búsqueda y ordenamiento (todos opcionales)
     * @return lista de productos que coinciden con los criterios aplicados
     */
    @GetMapping("/api/productos")
    public ResponseEntity<List<ProductoDto>> buscarProductos(
            @ModelAttribute FiltroProductoRequest filtro) {
        return ResponseEntity.ok(productoService.buscarProductos(filtro));
    }

    /**
     * Retorna el detalle completo de un producto, incluyendo la calificación promedio (REQ-15).
     * Endpoint público, no requiere autenticación.
     *
     * @param id identificador del producto
     * @return DTO con todos los datos del producto
     */
    @GetMapping("/api/productos/{id}")
    public ResponseEntity<ProductoDto> obtenerProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProducto(id));
    }

    /**
     * Lista los productos activos del vendedor autenticado con ordenamiento configurable.
     *
     * @param ordenarPor criterio de ordenamiento (precio_asc, precio_desc, nombre, fecha)
     * @param authHeader header Authorization con el token Bearer del vendedor
     * @return lista de productos activos del vendedor
     */
    @GetMapping("/api/seller/productos")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<List<ProductoDto>> misProductos(
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoVendedor = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(productoService.listarProductosVendedor(codigoVendedor, ordenarPor));
    }

    /**
     * Actualiza los datos de un producto. Solo el vendedor propietario puede modificarlo.
     * Si cambia la categoría, actualiza los contadores de ambas (REQ-04).
     *
     * @param id         identificador del producto a actualizar
     * @param request    nuevos datos del producto
     * @param imagen     nueva imagen opcional
     * @param authHeader header Authorization con el token Bearer del vendedor
     * @return DTO del producto actualizado
     */
    @PutMapping(value = "/api/seller/productos/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<ProductoDto> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestPart("datos") CrearProductoRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoVendedor = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(productoService.actualizarProducto(id, request, imagen, codigoVendedor));
    }

    /**
     * Realiza la eliminación lógica de un producto ({@code activoPub = false}).
     * Solo el vendedor propietario puede eliminarlo. Decrementa el contador de la categoría (REQ-04).
     *
     * @param id         identificador del producto a eliminar
     * @param authHeader header Authorization con el token Bearer del vendedor
     * @return HTTP 204 sin contenido
     */
    @DeleteMapping("/api/seller/productos/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoVendedor = jwtUtil.extractUserId(authHeader.substring(7));
        productoService.eliminarProducto(id, codigoVendedor);
        return ResponseEntity.noContent().build();
    }
}
