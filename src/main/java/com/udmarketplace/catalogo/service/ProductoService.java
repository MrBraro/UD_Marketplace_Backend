package com.udmarketplace.catalogo.service;

import com.udmarketplace.catalogo.dto.CrearProductoRequest;
import com.udmarketplace.catalogo.dto.FiltroProductoRequest;
import com.udmarketplace.catalogo.dto.ProductoDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Contrato del servicio de gestión de publicaciones del catálogo UD Marketplace.
 *
 * <p>Cubre el ciclo de vida completo de un producto: registro, consulta, actualización
 * y eliminación lógica. También gestiona la búsqueda con filtros dinámicos y el
 * ordenamiento por múltiples criterios.
 *
 * <p>Efectos secundarios de las operaciones de escritura:
 * <ul>
 *   <li>Registrar producto → incrementa el contador de la categoría </li>
 *   <li>Eliminar producto → decrementa el contador de la categoría </li>
 *   <li>Cambiar categoría → decrementa la anterior, incrementa la nueva </li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface ProductoService {

    /**
     * Registra un nuevo producto en el catálogo asociado al vendedor autenticado.
     * Actualiza el contador de la categoría al registrar.
     *
     * @param request       datos del producto a registrar
     * @param imagen        imagen opcional del producto (BLOB)
     * @param codigoVendedor identificador del vendedor propietario
     * @return DTO con los datos del producto creado
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si el vendedor o la categoría no existen
     */
    ProductoDto registrarProducto(CrearProductoRequest request, MultipartFile imagen, Long codigoVendedor);

    /**
     * Retorna el detalle completo de un producto activo por su identificador,
     * incluyendo la calificación promedio de sus valoraciones activas.
     *
     * @param idPub identificador del producto
     * @return DTO con todos los datos del producto
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si el producto no existe o está inactivo
     */
    ProductoDto obtenerProducto(Long idPub);

    /**
     * Lista los productos activos publicados por un vendedor específico,
     * ordenados según el criterio recibido (precio_asc, precio_desc, nombre, fecha).
     *
     * @param codigoVendedor identificador del vendedor
     * @param ordenarPor     criterio de ordenamiento (nullable, por defecto: fecha_desc)
     * @return lista de productos activos del vendedor
     */
    List<ProductoDto> listarProductosVendedor(Long codigoVendedor, String ordenarPor);

    /**
     * Busca productos aplicando los filtros recibidos (nombre, categoría, precio,
     * ubicación, disponibilidad) con ordenamiento dinámico (REQ catálogo).
     *
     * @param filtro parámetros de búsqueda y ordenamiento
     * @return lista de productos que coinciden con los filtros aplicados
     */
    List<ProductoDto> buscarProductos(FiltroProductoRequest filtro);

    /**
     * Actualiza los datos de un producto. Solo el vendedor propietario puede modificarlo.
     * Si cambia la categoría, actualiza los contadores de ambas.
     *
     * @param idPub          identificador del producto a actualizar
     * @param request        nuevos datos del producto
     * @param imagen         nueva imagen opcional (si es {@code null} no se modifica)
     * @param codigoVendedor identificador del vendedor que realiza la acción
     * @return DTO con los datos actualizados del producto
     * @throws com.udmarketplace.auth.exception.OperacionNoPermitidaException si el vendedor no es el propietario
     */
    ProductoDto actualizarProducto(Long idPub, CrearProductoRequest request, MultipartFile imagen, Long codigoVendedor);

    /**
     * Realiza la eliminación lógica de un producto ({@code activoPub = false}).
     * Solo el vendedor propietario puede eliminarlo. Decrementa el contador de la categoría.
     *
     * @param idPub          identificador del producto a eliminar
     * @param codigoVendedor identificador del vendedor que realiza la acción
     * @throws com.udmarketplace.auth.exception.OperacionNoPermitidaException si el vendedor no es el propietario
     */
    void eliminarProducto(Long idPub, Long codigoVendedor);
}
