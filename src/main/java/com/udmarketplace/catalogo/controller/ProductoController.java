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

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

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
@Tag(name = "Productos", description = "Endpoints para la publicación, consulta y gestión de productos en el catálogo")
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
    @Operation(
            summary = "Registrar nuevo producto",
            description = "Permite a un vendedor registrar un producto en el catálogo. Soporta subida de imagen adjunta. Actualiza contadores de categorías. Requiere rol VENDEDOR.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Producto registrado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - Permisos insuficientes")
            }
    )
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
    @Operation(
            summary = "Buscar productos con filtros",
            description = "Consulta y filtra productos activos en el catálogo. Soporta búsquedas por texto libre, rango de precio, categoría y ordenamiento. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente")
            }
    )
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
    @Operation(
            summary = "Obtener detalle del producto",
            description = "Retorna los detalles específicos de un producto mediante su identificador, incluyendo estadísticas de calificación. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Detalles del producto obtenidos exitosamente"),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
            }
    )
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
    @Operation(
            summary = "Listar productos del vendedor",
            description = "Retorna una lista de todas las publicaciones activas del vendedor autenticado. Requiere rol VENDEDOR.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de productos del vendedor obtenida exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - Permisos insuficientes")
            }
    )
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
    @Operation(
            summary = "Actualizar producto",
            description = "Permite al vendedor propietario de una publicación actualizar sus detalles y cambiar su imagen. Requiere rol VENDEDOR.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - Permisos insuficientes o no es el propietario"),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
            }
    )
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
    @Operation(
            summary = "Eliminar producto",
            description = "Realiza la eliminación lógica de un producto del catálogo. Decrementa contadores en categoría. Requiere rol VENDEDOR y ser el propietario.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - Permisos insuficientes o no es el propietario"),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
            }
    )
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long codigoVendedor = jwtUtil.extractUserId(authHeader.substring(7));
        productoService.eliminarProducto(id, codigoVendedor);
        return ResponseEntity.noContent().build();
    }
}
