/**
 * Pruebas unitarias para TransaccionServiceImpl.
 * Cubre registro de intención de compra, confirmación, consulta e historial filtrable.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.transaccion.service;

import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.model.Vendedor;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.catalogo.model.Categoria;
import com.udmarketplace.catalogo.model.Producto;
import com.udmarketplace.catalogo.repository.ProductoRepository;
import com.udmarketplace.transaccion.dto.CrearTransaccionRequest;
import com.udmarketplace.transaccion.dto.FiltroHistorialRequest;
import com.udmarketplace.transaccion.dto.TransaccionDto;
import com.udmarketplace.transaccion.model.DetalleOrdenEntrega;
import com.udmarketplace.transaccion.model.EstadoOrden;
import com.udmarketplace.transaccion.model.Orden;
import com.udmarketplace.transaccion.repository.DetalleOrdenEntregaRepository;
import com.udmarketplace.transaccion.repository.OrdenRepository;
import com.udmarketplace.transaccion.service.impl.TransaccionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceImplTest {

    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private DetalleOrdenEntregaRepository detalleRepo;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductoRepository productoRepository;
    @InjectMocks
    private TransaccionServiceImpl service;

    private User comprador(Long id) {
        User u = new User();
        u.setCodigoUsua(id);
        u.setPrimerNombre("María");
        u.setPrimerApellido("Torres");
        return u;
    }

    private Vendedor vendedor(Long id) {
        Vendedor v = new Vendedor();
        v.setCodigoUsua(id);
        v.setPrimerNombre("Pedro");
        v.setPrimerApellido("Gómez");
        return v;
    }

    private Producto productoDisponible(Long id, Vendedor vendedor) {
        return Producto.builder()
                .idPub(id)
                .nombrePub("Laptop")
                .descripcionPub("Laptop gaming")
                .precioPub(new BigDecimal("2500000"))
                .activoPub(true)
                .disponibilidad(true)
                .vendedor(vendedor)
                .categoria(Categoria.builder().idCategoria(1L).nombreCat("Tech").build())
                .build();
    }

    private Orden ordenConEstado(Long id, User comprador, Vendedor vendedor, Producto producto, String estado) {
        return Orden.builder()
                .idOrden(id)
                .comprador(comprador)
                .vendedor(vendedor)
                .producto(producto)
                .totalCompra(producto.getPrecioPub())
                .estadoOrden(estado)
                .fechaCompr(LocalDate.now())
                .datetimeCompra(LocalDateTime.now())
                .build();
    }

    // ------------------------------------------------------------------ registrarIntencioneCompra

    @Test
    void registrarIntencioneCompra_exitoso_asociaCompradorVendedorProducto() {
        User comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = productoDisponible(10L, vendedor);
        Orden guardada = ordenConEstado(100L, comprador, vendedor, producto, EstadoOrden.PENDIENTE.name());

        when(userRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(ordenRepository.save(any())).thenReturn(guardada);

        CrearTransaccionRequest request = new CrearTransaccionRequest();
        request.setIdPub(10L);

        TransaccionDto result = service.registrarIntencioneCompra(request, 1L);

        assertThat(result.getIdOrden()).isEqualTo(100L);
        assertThat(result.getEstadoOrden()).isEqualTo(EstadoOrden.PENDIENTE.name());
        assertThat(result.getIdComprador()).isEqualTo(1L);
        assertThat(result.getIdVendedor()).isEqualTo(2L);
        assertThat(result.getIdProducto()).isEqualTo(10L);
    }

    @Test
    void registrarIntencioneCompra_compradorNoEncontrado_lanzaExcepcion() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        CrearTransaccionRequest request = new CrearTransaccionRequest();
        request.setIdPub(10L);
        assertThatThrownBy(() -> service.registrarIntencioneCompra(request, 99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(ordenRepository, never()).save(any());
    }

    @Test
    void registrarIntencioneCompra_productoInactivo_lanzaExcepcion() {
        User comprador = comprador(1L);
        Producto inactivo = Producto.builder()
                .idPub(10L).activoPub(false).disponibilidad(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(inactivo));

        CrearTransaccionRequest request = new CrearTransaccionRequest();
        request.setIdPub(10L);

        assertThatThrownBy(() -> service.registrarIntencioneCompra(request, 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void registrarIntencioneCompra_productoSinDisponibilidad_lanzaExcepcion() {
        User comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto noDisponible = Producto.builder()
                .idPub(10L).activoPub(true).disponibilidad(false).vendedor(vendedor).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(noDisponible));

        CrearTransaccionRequest request = new CrearTransaccionRequest();
        request.setIdPub(10L);

        assertThatThrownBy(() -> service.registrarIntencioneCompra(request, 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------ confirmarTransaccion

    @Test
    void confirmarTransaccion_exitoso_generaOrdenEntrega() {
        User comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = productoDisponible(10L, vendedor);
        Orden orden = ordenConEstado(100L, comprador, vendedor, producto, EstadoOrden.PENDIENTE.name());

        when(ordenRepository.findByIdOrdenAndVendedor_CodigoUsua(100L, 2L))
                .thenReturn(Optional.of(orden));
        when(ordenRepository.save(any())).thenReturn(orden);
        when(detalleRepo.save(any())).thenAnswer(inv -> {
            DetalleOrdenEntrega d = inv.getArgument(0);
            d.setIdDetalle(1L);
            return d;
        });

        TransaccionDto result = service.confirmarTransaccion(100L, 2L);

        assertThat(result.getEstadoOrden()).isEqualTo(EstadoOrden.CONFIRMADA.name());
        verify(detalleRepo).save(any(DetalleOrdenEntrega.class));
    }

    @Test
    void confirmarTransaccion_confirmacionDigitalTieneFormato() {
        User comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = productoDisponible(10L, vendedor);
        Orden orden = ordenConEstado(100L, comprador, vendedor, producto, EstadoOrden.PENDIENTE.name());

        when(ordenRepository.findByIdOrdenAndVendedor_CodigoUsua(100L, 2L))
                .thenReturn(Optional.of(orden));
        when(ordenRepository.save(any())).thenReturn(orden);
        when(detalleRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.confirmarTransaccion(100L, 2L);

        // Verificar que el detalle se guardó con confirmación digital en el formato esperado
        verify(detalleRepo).save(argThat(d ->
                d.getConfirmacionDigital() != null &&
                d.getConfirmacionDigital().startsWith("CONF-")
        ));
    }

    @Test
    void confirmarTransaccion_estadoNoPendiente_lanzaExcepcion() {
        User comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = productoDisponible(10L, vendedor);
        Orden yaConfirmada = ordenConEstado(100L, comprador, vendedor, producto, EstadoOrden.CONFIRMADA.name());

        when(ordenRepository.findByIdOrdenAndVendedor_CodigoUsua(100L, 2L))
                .thenReturn(Optional.of(yaConfirmada));

        assertThatThrownBy(() -> service.confirmarTransaccion(100L, 2L))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("PENDIENTE");
    }

    @Test
    void confirmarTransaccion_vendedorIncorrecto_lanzaExcepcion() {
        when(ordenRepository.findByIdOrdenAndVendedor_CodigoUsua(100L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmarTransaccion(100L, 99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(detalleRepo, never()).save(any());
    }

    // ------------------------------------------------------------------ obtenerTransaccion

    @Test
    void obtenerTransaccion_exitoso() {
        User comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = productoDisponible(10L, vendedor);
        Orden orden = ordenConEstado(100L, comprador, vendedor, producto, EstadoOrden.PENDIENTE.name());

        when(ordenRepository.findById(100L)).thenReturn(Optional.of(orden));

        TransaccionDto result = service.obtenerTransaccion(100L);

        assertThat(result.getIdOrden()).isEqualTo(100L);
        assertThat(result.getNombreProducto()).isEqualTo("Laptop");
    }

    @Test
    void obtenerTransaccion_noEncontrada_lanzaExcepcion() {
        when(ordenRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtenerTransaccion(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------ consultarHistorial

    @Test
    void consultarHistorial_devuelveListaFiltrada() {
        User comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = productoDisponible(10L, vendedor);
        Orden o1 = ordenConEstado(100L, comprador, vendedor, producto, EstadoOrden.CONFIRMADA.name());
        Orden o2 = ordenConEstado(101L, comprador, vendedor, producto, EstadoOrden.PENDIENTE.name());

        when(ordenRepository.buscarHistorial(1L, null, null, null, null))
                .thenReturn(List.of(o1, o2));

        FiltroHistorialRequest filtro = new FiltroHistorialRequest();
        filtro.setCodigoComprador(1L);

        List<TransaccionDto> result = service.consultarHistorial(filtro);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIdOrden()).isEqualTo(100L);
        assertThat(result.get(1).getEstadoOrden()).isEqualTo(EstadoOrden.PENDIENTE.name());
    }

    @Test
    void consultarHistorial_sinResultados_devuelveListaVacia() {
        when(ordenRepository.buscarHistorial(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        List<TransaccionDto> result = service.consultarHistorial(new FiltroHistorialRequest());

        assertThat(result).isEmpty();
    }
}
