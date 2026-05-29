/**
 * Pruebas unitarias para ProductoServiceImpl.
 * Cubre registro, consulta, actualización, eliminación lógica y búsqueda con filtros.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.catalogo.service;

import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.model.Vendedor;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.catalogo.dto.CrearProductoRequest;
import com.udmarketplace.catalogo.dto.FiltroProductoRequest;
import com.udmarketplace.catalogo.dto.ProductoDto;
import com.udmarketplace.catalogo.model.Categoria;
import com.udmarketplace.catalogo.model.Producto;
import com.udmarketplace.catalogo.repository.CategoriaRepository;
import com.udmarketplace.catalogo.repository.ProductoRepository;
import com.udmarketplace.catalogo.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoriaService categoriaService;
    @InjectMocks
    private ProductoServiceImpl service;

    private Vendedor vendedor(Long id) {
        Vendedor v = new Vendedor();
        v.setCodigoUsua(id);
        v.setPrimerNombre("Ana");
        v.setPrimerApellido("López");
        return v;
    }

    private Categoria categoriaActiva(Long id) {
        return Categoria.builder()
                .idCategoria(id)
                .nombreCat("Electrónica")
                .activoCat(true)
                .contadorProductos(0)
                .build();
    }

    private Producto productoActivo(Long id, Vendedor vendedor, Categoria categoria) {
        return Producto.builder()
                .idPub(id)
                .nombrePub("Laptop")
                .descripcionPub("Laptop gaming")
                .precioPub(new BigDecimal("2500000"))
                .activoPub(true)
                .disponibilidad(true)
                .vendedor(vendedor)
                .categoria(categoria)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    private CrearProductoRequest request(Long idCategoria) {
        CrearProductoRequest r = new CrearProductoRequest();
        r.setNombrePub("Laptop");
        r.setDescripcionPub("Laptop gaming");
        r.setPrecioPub(new BigDecimal("2500000"));
        r.setIdCategoria(idCategoria);
        r.setDisponibilidad(true);
        return r;
    }

    // ------------------------------------------------------------------ registrarProducto

    @Test
    void registrarProducto_exitoso_incrementaContador() {
        Vendedor vendedor = vendedor(1L);
        Categoria categoria = categoriaActiva(2L);
        Producto guardado = productoActivo(10L, vendedor, categoria);

        when(userRepository.findById(1L)).thenReturn(Optional.of(vendedor));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any())).thenReturn(guardado);

        ProductoDto result = service.registrarProducto(request(2L), null, 1L);

        assertThat(result.getIdPub()).isEqualTo(10L);
        assertThat(result.getNombrePub()).isEqualTo("Laptop");
        assertThat(result.getIdVendedor()).isEqualTo(1L);
        verify(categoriaService).incrementarContador(2L);
    }

    @Test
    void registrarProducto_vendedorNoEncontrado_lanzaExcepcion() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.registrarProducto(request(1L), null, 99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void registrarProducto_categoriaInactiva_lanzaExcepcion() {
        Vendedor vendedor = vendedor(1L);
        Categoria inactiva = Categoria.builder().idCategoria(5L).activoCat(false).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(vendedor));
        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(inactiva));

        assertThatThrownBy(() -> service.registrarProducto(request(5L), null, 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(categoriaService, never()).incrementarContador(anyLong());
    }

    @Test
    void registrarProducto_categoriaNoExiste_lanzaExcepcion() {
        Vendedor vendedor = vendedor(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(vendedor));
        when(categoriaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarProducto(request(9L), null, 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------ obtenerProducto

    @Test
    void obtenerProducto_exitoso_inclueyeCalificacion() {
        Vendedor vendedor = vendedor(1L);
        Categoria categoria = categoriaActiva(2L);
        Producto producto = productoActivo(10L, vendedor, categoria);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.calcularCalificacionPromedio(10L)).thenReturn(4.5);

        ProductoDto result = service.obtenerProducto(10L);

        assertThat(result.getIdPub()).isEqualTo(10L);
        assertThat(result.getCalificacionPromedio()).isEqualTo(4.5);
    }

    @Test
    void obtenerProducto_inactivo_lanzaExcepcion() {
        Producto inactivo = Producto.builder().idPub(10L).activoPub(false).build();
        when(productoRepository.findById(10L)).thenReturn(Optional.of(inactivo));
        assertThatThrownBy(() -> service.obtenerProducto(10L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void obtenerProducto_noExiste_lanzaExcepcion() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtenerProducto(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------ listarProductosVendedor

    @Test
    void listarProductosVendedor_devuelveLista() {
        Vendedor vendedor = vendedor(1L);
        Categoria categoria = categoriaActiva(2L);
        List<Producto> productos = List.of(
                productoActivo(1L, vendedor, categoria),
                productoActivo(2L, vendedor, categoria)
        );

        when(productoRepository.findByVendedor_CodigoUsuaAndActivoPubTrue(eq(1L), any(Sort.class)))
                .thenReturn(productos);

        List<ProductoDto> result = service.listarProductosVendedor(1L, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void listarProductosVendedor_ordenPrecioAsc() {
        Vendedor vendedor = vendedor(1L);
        when(productoRepository.findByVendedor_CodigoUsuaAndActivoPubTrue(eq(1L), any(Sort.class)))
                .thenReturn(List.of());

        List<ProductoDto> result = service.listarProductosVendedor(1L, "precio_asc");

        assertThat(result).isEmpty();
        verify(productoRepository).findByVendedor_CodigoUsuaAndActivoPubTrue(eq(1L), any(Sort.class));
    }

    // ------------------------------------------------------------------ buscarProductos

    @Test
    @SuppressWarnings("unchecked")
    void buscarProductos_sinFiltros_devuelveActivos() {
        Vendedor vendedor = vendedor(1L);
        Categoria categoria = categoriaActiva(2L);

        when(productoRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(productoActivo(1L, vendedor, categoria)));

        List<ProductoDto> result = service.buscarProductos(new FiltroProductoRequest());

        assertThat(result).hasSize(1);
    }

    // ------------------------------------------------------------------ actualizarProducto

    @Test
    void actualizarProducto_cambiaCategoria_actualizaAmbosContadores() {
        Vendedor vendedor = vendedor(1L);
        Categoria categoriaAnterior = categoriaActiva(2L);
        Categoria categoriaNueva = categoriaActiva(3L);
        Producto producto = productoActivo(10L, vendedor, categoriaAnterior);

        CrearProductoRequest requestActualizar = request(3L);
        requestActualizar.setNombrePub("Laptop Actualizada");

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoriaNueva));
        when(productoRepository.save(any())).thenReturn(producto);

        service.actualizarProducto(10L, requestActualizar, null, 1L);

        verify(categoriaService).decrementarContador(2L);
        verify(categoriaService).incrementarContador(3L);
    }

    @Test
    void actualizarProducto_mismaCategoria_noActualizaContadores() {
        Vendedor vendedor = vendedor(1L);
        Categoria categoria = categoriaActiva(2L);
        Producto producto = productoActivo(10L, vendedor, categoria);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any())).thenReturn(producto);

        service.actualizarProducto(10L, request(2L), null, 1L);

        verify(categoriaService, never()).decrementarContador(anyLong());
        verify(categoriaService, never()).incrementarContador(anyLong());
    }

    @Test
    void actualizarProducto_noEsPropietario_lanzaExcepcion() {
        Vendedor vendedor = vendedor(1L);
        Categoria categoria = categoriaActiva(2L);
        Producto producto = productoActivo(10L, vendedor, categoria);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> service.actualizarProducto(10L, request(2L), null, 99L))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    // ------------------------------------------------------------------ eliminarProducto

    @Test
    void eliminarProducto_softDelete_decrementaContador() {
        Vendedor vendedor = vendedor(1L);
        Categoria categoria = categoriaActiva(2L);
        Producto producto = productoActivo(10L, vendedor, categoria);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any())).thenReturn(producto);

        service.eliminarProducto(10L, 1L);

        assertThat(producto.isActivoPub()).isFalse();
        verify(categoriaService).decrementarContador(2L);
    }

    @Test
    void eliminarProducto_noEsPropietario_lanzaExcepcion() {
        Vendedor vendedor = vendedor(1L);
        Categoria categoria = categoriaActiva(2L);
        Producto producto = productoActivo(10L, vendedor, categoria);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> service.eliminarProducto(10L, 99L))
                .isInstanceOf(OperacionNoPermitidaException.class);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void eliminarProducto_noExiste_lanzaExcepcion() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.eliminarProducto(99L, 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
