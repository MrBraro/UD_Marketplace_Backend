package com.udmarketplace.catalogo.service.impl;

import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.model.Vendedor;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.service.FileValidationService;
import com.udmarketplace.catalogo.dto.CrearProductoRequest;
import com.udmarketplace.catalogo.dto.FiltroProductoRequest;
import com.udmarketplace.catalogo.dto.ProductoDto;
import com.udmarketplace.catalogo.model.Categoria;
import com.udmarketplace.catalogo.model.Producto;
import com.udmarketplace.catalogo.repository.CategoriaRepository;
import com.udmarketplace.catalogo.repository.ProductoRepository;
import com.udmarketplace.catalogo.service.CategoriaService;
import com.udmarketplace.catalogo.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de gestión de publicaciones del catálogo UD Marketplace.
 *
 * <p>Implementa el ciclo de vida completo de los productos con actualización automática
 * del contador de categorías, búsqueda dinámica mediante JPA Specifications,
 * ordenamiento configurable, validación de imágenes (RNF08) y soft-delete.

 * <p>Todas las operaciones de escritura están anotadas con {@code @Transactional}
 * para garantizar la reversión ante fallos.
 *
 * @author Daniel Perez
 * @version 1.1
 * @since 2026-05-28
 */
@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    /** Repositorio de productos para operaciones CRUD y búsqueda por especificación. */
    private final ProductoRepository productoRepository;

    /** Repositorio de categorías para validar que existen y están activas. */
    private final CategoriaRepository categoriaRepository;

    /** Repositorio de usuarios para obtener la entidad Vendedor. */
    private final UserRepository userRepository;

    /** Servicio de categorías para actualizar el contador de productos (REQ-04). */
    private final CategoriaService categoriaService;

    /** Servicio de validación de archivos para validar imágenes (RNF08). */
    private final FileValidationService fileValidationService;


    /**
     * {@inheritDoc}
     *
     * <p>Persiste la imagen como BLOB si se provee. Incrementa el contador de la categoría (REQ-04).
     * Valida que la imagen sea un formato soportado, MIME type correcto y tamaño dentro del límite (RNF08).
     */
    @Override
    @Transactional
    public ProductoDto registrarProducto(CrearProductoRequest request, MultipartFile imagen, Long codigoVendedor) {
        Vendedor vendedor = obtenerVendedor(codigoVendedor);
        Categoria categoria = obtenerCategoria(request.getIdCategoria());

        Producto producto = Producto.builder()
                .vendedor(vendedor)
                .nombrePub(request.getNombrePub())
                .descripcionPub(request.getDescripcionPub())
                .precioPub(request.getPrecioPub())
                .ubicacion(request.getUbicacion())
                .condicionesVenta(request.getCondicionesVenta())
                .disponibilidad(request.isDisponibilidad())
                .categoria(categoria)
                .fechaRegistro(LocalDateTime.now())
                .activoPub(true)
                .build();

        if (imagen != null && !imagen.isEmpty()) {
            // RNF08: Validar tipo MIME, extensión y tamaño de la imagen
            fileValidationService.validateImage(imagen);
            try {
                producto.setImagenPub(imagen.getBytes());
            } catch (IOException e) {
                throw new OperacionNoPermitidaException("Error al procesar la imagen");
            }
        }

        Producto guardado = productoRepository.save(producto);
        // REQ-04: actualizar contador de la categoría
        categoriaService.incrementarContador(categoria.getIdCategoria());
        return toDto(guardado);
    }

    /** {@inheritDoc} */
    @Override
    public ProductoDto obtenerProducto(Long idPub) {
        Producto producto = buscarProductoActivo(idPub);
        Double calificacion = productoRepository.calcularCalificacionPromedio(idPub);
        ProductoDto dto = toDto(producto);
        dto.setCalificacionPromedio(calificacion);
        return dto;
    }

    /** {@inheritDoc} */
    @Override
    public List<ProductoDto> listarProductosVendedor(Long codigoVendedor, String ordenarPor) {
        Sort sort = resolverOrden(ordenarPor);
        return productoRepository.findByVendedor_CodigoUsuaAndActivoPubTrue(codigoVendedor, sort)
                .stream().map(this::toDto).toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<ProductoDto> buscarProductos(FiltroProductoRequest filtro) {
        Specification<Producto> spec = construirFiltro(filtro);
        Sort sort = resolverOrden(filtro.getOrdenarPor());
        return productoRepository.findAll(spec, sort).stream().map(this::toDto).toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Actualiza los contadores de las categorías si el producto cambia de categoría (REQ-04).
     */
    @Override
    @Transactional
    public ProductoDto actualizarProducto(Long idPub, CrearProductoRequest request, MultipartFile imagen,
                                          Long codigoVendedor) {
        Producto producto = buscarProductoActivo(idPub);
        validarPropietario(producto, codigoVendedor);

        Long categoriaAnteriorId = producto.getCategoria() != null ? producto.getCategoria().getIdCategoria() : null;

        producto.setNombrePub(request.getNombrePub());
        producto.setDescripcionPub(request.getDescripcionPub());
        producto.setPrecioPub(request.getPrecioPub());
        producto.setUbicacion(request.getUbicacion());
        producto.setCondicionesVenta(request.getCondicionesVenta());
        producto.setDisponibilidad(request.isDisponibilidad());

        Categoria nuevaCategoria = obtenerCategoria(request.getIdCategoria());
        producto.setCategoria(nuevaCategoria);

        validarImagen(imagen);

        if (imagen != null && !imagen.isEmpty()) {
            // RNF08: Validar tipo MIME, extensión y tamaño de la imagen
            fileValidationService.validateImage(imagen);
            try {
                producto.setImagenPub(imagen.getBytes());
            } catch (IOException e) {
                throw new OperacionNoPermitidaException("Error al procesar la imagen");
            }
        }

        // REQ-04: actualizar contadores si cambió la categoría
        if (categoriaAnteriorId != null && !categoriaAnteriorId.equals(nuevaCategoria.getIdCategoria())) {
            categoriaService.decrementarContador(categoriaAnteriorId);
            categoriaService.incrementarContador(nuevaCategoria.getIdCategoria());
        }

        return toDto(productoRepository.save(producto));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Realiza soft-delete ({@code activoPub = false}) y decrementa el contador de la categoría (REQ-04).
     */
    @Override
    @Transactional
    public void eliminarProducto(Long idPub, Long codigoVendedor) {
        Producto producto = buscarProductoActivo(idPub);
        validarPropietario(producto, codigoVendedor);
        producto.setActivoPub(false);
        productoRepository.save(producto);
        // REQ-04: decrementar contador al inactivar
        if (producto.getCategoria() != null) {
            categoriaService.decrementarContador(producto.getCategoria().getIdCategoria());
        }
    }

    // ------------------------------------------------------------------
    // Métodos privados
    // ------------------------------------------------------------------

    /**
     * Obtiene la entidad {@link Vendedor} por ID o lanza excepción si no existe.
     *
     * @param codigoVendedor identificador del vendedor
     * @return entidad Vendedor
     */
    private Vendedor obtenerVendedor(Long codigoVendedor) {
        return (Vendedor) userRepository.findById(codigoVendedor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vendedor no encontrado"));
    }

    /**
     * Obtiene una categoría activa por ID o lanza excepción si no existe o está inactiva.
     *
     * @param idCategoria identificador de la categoría
     * @return entidad Categoria activa
     */
    private Categoria obtenerCategoria(Long idCategoria) {
        return categoriaRepository.findById(idCategoria)
                .filter(Categoria::isActivoCat)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada o inactiva: " + idCategoria));
    }

    /**
     * Busca un producto activo por ID o lanza excepción si no existe o está inactivo.
     *
     * @param idPub identificador del producto
     * @return entidad Producto activa
     */
    private Producto buscarProductoActivo(Long idPub) {
        return productoRepository.findById(idPub)
                .filter(Producto::isActivoPub)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + idPub));
    }

    /**
     * Verifica que el vendedor sea el propietario del producto. Lanza excepción si no coincide.
     *
     * @param producto       producto a verificar
     * @param codigoVendedor identificador del vendedor que intenta la operación
     * @throws com.udmarketplace.auth.exception.OperacionNoPermitidaException si no es el propietario
     */
    private void validarPropietario(Producto producto, Long codigoVendedor) {
        if (!producto.getVendedor().getCodigoUsua().equals(codigoVendedor)) {
            throw new OperacionNoPermitidaException("No tiene permisos para modificar este producto");
        }
    }

   /**
     * Valida la imagen enviada.
     *
     * <p>Permite JPG, PNG o WEBP con tamaño máximo de 5 MB.
     *
     * @param imagen archivo de imagen
     */
    private void validarImagen(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            return;
        }

        String contentType = imagen.getContentType();
        if (contentType == null ||
                (!contentType.equalsIgnoreCase("image/jpeg")
                        && !contentType.equalsIgnoreCase("image/png")
                        && !contentType.equalsIgnoreCase("image/webp"))) {
            throw new OperacionNoPermitidaException("Formato de imagen no permitido. Use JPG, PNG o WEBP");
        }

        long maxBytes = 5 * 1024 * 1024;
        if (imagen.getSize() > maxBytes) {
            throw new OperacionNoPermitidaException("La imagen supera el tamaño máximo permitido de 5 MB");
        }
    }
    /**
     * Resuelve el criterio de ordenamiento textual a un objeto {@link Sort} de Spring Data.
     * Valores soportados: {@code precio_asc}, {@code precio_desc}, {@code nombre}.
     * Cualquier otro valor o {@code null} usa ordenamiento por fecha descendente.
     *
     * @param criterio criterio de ordenamiento recibido como parámetro de consulta
     * @return objeto Sort configurado para la consulta
     */
/**
 * Resuelve el criterio de ordenamiento solicitado a una instancia de {@link Sort}.
 *
 * @param criterio texto de ordenamiento recibido
 * @return criterio de ordenamiento aplicado
 */
private Sort resolverOrden(String criterio) {
    if (criterio == null) {
        return Sort.by(Sort.Direction.DESC, "fechaRegistro");
    }

    switch (criterio.toLowerCase()) {
        case "precio_asc":
            return Sort.by(Sort.Direction.ASC, "precioPub");
        case "precio_desc":
            return Sort.by(Sort.Direction.DESC, "precioPub");
        case "nombre":
            return Sort.by(Sort.Direction.ASC, "nombrePub");
        default:
            return Sort.by(Sort.Direction.DESC, "fechaRegistro");
    }
}

    /**
     * Construye una especificación JPA dinámica a partir de los filtros de búsqueda.
     * Solo incluye predicados para los filtros que no son nulos o vacíos.
     * Siempre incluye el filtro {@code activoPub = true} para excluir productos eliminados.
     *
     * @param f DTO con los parámetros de filtrado
     * @return especificación JPA combinada con AND
     */
    private Specification<Producto> construirFiltro(FiltroProductoRequest f) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.isTrue(root.get("activoPub")));

            predicates.add(cb.isTrue(root.get("activoPub")));
            if (f.getNombre() != null && !f.getNombre().isBlank())
                predicates.add(cb.like(cb.lower(root.get("nombrePub")), "%" + f.getNombre().toLowerCase() + "%"));
            if (f.getIdCategoria() != null)
                predicates.add(cb.equal(root.get("categoria").get("idCategoria"), f.getIdCategoria()));
            if (f.getPrecioMin() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("precioPub"), f.getPrecioMin()));
            if (f.getPrecioMax() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("precioPub"), f.getPrecioMax()));
            if (f.getUbicacion() != null && !f.getUbicacion().isBlank())
                predicates.add(cb.like(cb.lower(root.get("ubicacion")), "%" + f.getUbicacion().toLowerCase() + "%"));
            if (f.getDisponibilidad() != null)
                predicates.add(cb.equal(root.get("disponibilidad"), f.getDisponibilidad()));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * Convierte una entidad {@link Producto} a su DTO de respuesta.
     * La calificación promedio no se incluye aquí; se agrega por separado en {@link #obtenerProducto}.
     *
     * @param p entidad del producto
     * @return DTO con los datos del producto
     */
    private ProductoDto toDto(Producto p) {
         String nombreVendedor = p.getVendedor().getPrimerNombre() + " " + p.getVendedor().getPrimerApellido();
        return ProductoDto.builder()
                .idPub(p.getIdPub())
                .nombrePub(p.getNombrePub())
                .descripcionPub(p.getDescripcionPub())
                .precioPub(p.getPrecioPub())
                .ubicacion(p.getUbicacion())
                .condicionesVenta(p.getCondicionesVenta())
                .disponibilidad(p.isDisponibilidad())
                .activoPub(p.isActivoPub())
                .fechaRegistro(p.getFechaRegistro())
                .idCategoria(p.getCategoria() != null ? p.getCategoria().getIdCategoria() : null)
                .nombreCategoria(p.getCategoria() != null ? p.getCategoria().getNombreCat() : null)
                .idVendedor(p.getVendedor().getCodigoUsua())
                .nombreVendedor(p.getVendedor().getPrimerNombre() + " " + p.getVendedor().getPrimerApellido())
                .build();
    }
}
