/**
 * Pruebas unitarias para ValoracionServiceImpl.
 * Cubre registro de valoraciones, historial sin sobrescritura, cálculo de reputación
 * y conteo de reseñas positivas del vendedor.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.valoracion.service;

import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.model.Comprador;
import com.udmarketplace.auth.model.Vendedor;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.catalogo.model.Categoria;
import com.udmarketplace.catalogo.model.Producto;
import com.udmarketplace.catalogo.repository.ProductoRepository;
import com.udmarketplace.transaccion.model.EstadoOrden;
import com.udmarketplace.transaccion.model.Orden;
import com.udmarketplace.transaccion.repository.OrdenRepository;
import com.udmarketplace.valoracion.dto.CrearValoracionRequest;
import com.udmarketplace.valoracion.dto.ReputacionVendedorDto;
import com.udmarketplace.valoracion.dto.ValoracionDto;
import com.udmarketplace.valoracion.model.Valoracion;
import com.udmarketplace.valoracion.repository.ResenaPredefinidaRepository;
import com.udmarketplace.valoracion.repository.ValoracionRepository;
import com.udmarketplace.valoracion.service.impl.ValoracionServiceImpl;
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
class ValoracionServiceImplTest {

    @Mock
    private ValoracionRepository valoracionRepository;
    @Mock
    private ResenaPredefinidaRepository resenaRepo;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private OrdenRepository ordenRepository;
    @InjectMocks
    private ValoracionServiceImpl service;

    private Comprador comprador(Long id) {
        Comprador c = new Comprador();
        c.setCodigoUsua(id);
        c.setPrimerNombre("Laura");
        c.setPrimerApellido("Martínez");
        return c;
    }

    private Vendedor vendedor(Long id) {
        Vendedor v = new Vendedor();
        v.setCodigoUsua(id);
        v.setPrimerNombre("Andrés");
        v.setPrimerApellido("Ruiz");
        v.setCalificacion(BigDecimal.ZERO);
        return v;
    }

    private Producto producto(Long id, Vendedor vendedor) {
        return Producto.builder()
                .idPub(id)
                .nombrePub("Laptop")
                .descripcionPub("Laptop gaming")
                .activoPub(true)
                .vendedor(vendedor)
                .categoria(Categoria.builder().idCategoria(1L).nombreCat("Tech").build())
                .build();
    }

    private Orden ordenConfirmada(Long id, Comprador comprador, Producto producto) {
        return Orden.builder()
                .idOrden(id)
                .comprador(comprador)
                .vendedor((Vendedor) producto.getVendedor())
                .producto(producto)
                .estadoOrden(EstadoOrden.CONFIRMADA.name())
                .datetimeCompra(LocalDateTime.now())
                .build();
    }

    private CrearValoracionRequest requestValoracion(Long idPub, Long idOrden, int calificacion) {
        CrearValoracionRequest r = new CrearValoracionRequest();
        r.setIdPub(idPub);
        r.setIdOrden(idOrden);
        r.setCalificacion(calificacion);
        return r;
    }

    // ------------------------------------------------------------------ registrarValoracion

    @Test
    void registrarValoracion_sinDuplicado_exitoso() {
        Comprador comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = producto(10L, vendedor);
        Orden orden = ordenConfirmada(100L, comprador, producto);

        when(userRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(ordenRepository.findById(100L)).thenReturn(Optional.of(orden));
        when(valoracionRepository.existsByComprador_CodigoUsuaAndProducto_IdPubAndEstadoValoTrue(1L, 10L))
                .thenReturn(false);

        Valoracion guardada = Valoracion.builder()
                .idVal(1L).comprador(comprador).vendedor(vendedor).producto(producto)
                .orden(orden).calificacion(5).fechaValo(LocalDate.now()).estadoValo(true).build();

        when(valoracionRepository.save(any())).thenReturn(guardada);
        when(valoracionRepository.calcularReputacionVendedor(2L)).thenReturn(5.0);
        when(userRepository.save(any())).thenReturn(vendedor);

        ValoracionDto result = service.registrarValoracion(requestValoracion(10L, 100L, 5), 1L);

        assertThat(result.getIdVal()).isEqualTo(1L);
        assertThat(result.getCalificacion()).isEqualTo(5);
        assertThat(result.isEstadoValo()).isTrue();
        verify(userRepository).save(vendedor); // reputación actualizada
    }

    @Test
    void registrarValoracion_compradorNoEncontrado_lanzaExcepcion() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.registrarValoracion(requestValoracion(10L, 100L, 4), 99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(valoracionRepository, never()).save(any());
    }

    @Test
    void registrarValoracion_productoNoEncontrado_lanzaExcepcion() {
        Comprador comprador = comprador(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarValoracion(requestValoracion(99L, 100L, 4), 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void registrarValoracion_ordenNoEncontrada_lanzaExcepcion() {
        Comprador comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = producto(10L, vendedor);

        when(userRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(ordenRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarValoracion(requestValoracion(10L, 99L, 4), 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void registrarValoracion_ordenDeOtroComprador_lanzaExcepcion() {
        Comprador comprador = comprador(1L);
        Comprador otroComprador = comprador(3L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = producto(10L, vendedor);

        Orden ordenAjena = Orden.builder()
                .idOrden(100L).comprador(otroComprador).producto(producto)
                .estadoOrden(EstadoOrden.CONFIRMADA.name()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(ordenRepository.findById(100L)).thenReturn(Optional.of(ordenAjena));

        assertThatThrownBy(() -> service.registrarValoracion(requestValoracion(10L, 100L, 4), 1L))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    void registrarValoracion_ordenNoPendiente_noConfirmada_lanzaExcepcion() {
        Comprador comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = producto(10L, vendedor);

        Orden pendiente = Orden.builder()
                .idOrden(100L).comprador(comprador).producto(producto)
                .estadoOrden(EstadoOrden.PENDIENTE.name()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(ordenRepository.findById(100L)).thenReturn(Optional.of(pendiente));

        assertThatThrownBy(() -> service.registrarValoracion(requestValoracion(10L, 100L, 4), 1L))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("confirmada");
    }

    @Test
    void registrarValoracion_duplicado_inactivaPreviaYCreaNueva() {
        Comprador comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = producto(10L, vendedor);
        Orden orden = ordenConfirmada(100L, comprador, producto);

        Valoracion previa = Valoracion.builder()
                .idVal(5L).comprador(comprador).vendedor(vendedor).producto(producto)
                .calificacion(3).estadoValo(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(comprador));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(ordenRepository.findById(100L)).thenReturn(Optional.of(orden));
        when(valoracionRepository.existsByComprador_CodigoUsuaAndProducto_IdPubAndEstadoValoTrue(1L, 10L))
                .thenReturn(true);
        when(valoracionRepository.findByComprador_CodigoUsuaAndProducto_IdPub(1L, 10L))
                .thenReturn(List.of(previa));

        Valoracion nueva = Valoracion.builder()
                .idVal(6L).comprador(comprador).vendedor(vendedor).producto(producto)
                .orden(orden).calificacion(5).fechaValo(LocalDate.now()).estadoValo(true).build();

        when(valoracionRepository.save(any())).thenReturn(nueva);
        when(valoracionRepository.calcularReputacionVendedor(2L)).thenReturn(5.0);
        when(userRepository.save(any())).thenReturn(vendedor);

        service.registrarValoracion(requestValoracion(10L, 100L, 5), 1L);

        // La valoración previa debe quedar inactiva (historial sin sobrescritura REQ-17)
        assertThat(previa.isEstadoValo()).isFalse();
        verify(valoracionRepository, atLeastOnce()).save(previa);
    }

    // ------------------------------------------------------------------ calcularPromedioProducto

    @Test
    void calcularPromedioProducto_delegaAlRepositorio() {
        when(valoracionRepository.calcularPromedioProducto(10L)).thenReturn(4.2);
        assertThat(service.calcularPromedioProducto(10L)).isEqualTo(4.2);
    }

    @Test
    void calcularPromedioProducto_sinValoraciones_retornaNull() {
        when(valoracionRepository.calcularPromedioProducto(10L)).thenReturn(null);
        assertThat(service.calcularPromedioProducto(10L)).isNull();
    }

    // ------------------------------------------------------------------ obtenerReputacionVendedor

    @Test
    void obtenerReputacionVendedor_calculos_correctos() {
        Vendedor vendedor = vendedor(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(vendedor));
        when(valoracionRepository.calcularReputacionVendedor(2L)).thenReturn(4.5);
        when(valoracionRepository.contarResenasPositivas(2L)).thenReturn(3L);
        when(valoracionRepository.findByVendedor_CodigoUsuaAndEstadoValoTrue(2L))
                .thenReturn(List.of(new Valoracion(), new Valoracion(), new Valoracion(), new Valoracion()));

        ReputacionVendedorDto result = service.obtenerReputacionVendedor(2L);

        assertThat(result.getIdVendedor()).isEqualTo(2L);
        assertThat(result.getCalificacionPromedio()).isEqualTo(4.5);
        assertThat(result.getTotalResenasPositivas()).isEqualTo(3L); // REQ-19
        assertThat(result.getTotalValoraciones()).isEqualTo(4L);
    }

    @Test
    void obtenerReputacionVendedor_sinValoraciones_promedioEnCero() {
        Vendedor vendedor = vendedor(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(vendedor));
        when(valoracionRepository.calcularReputacionVendedor(2L)).thenReturn(null);
        when(valoracionRepository.contarResenasPositivas(2L)).thenReturn(0L);
        when(valoracionRepository.findByVendedor_CodigoUsuaAndEstadoValoTrue(2L))
                .thenReturn(List.of());

        ReputacionVendedorDto result = service.obtenerReputacionVendedor(2L);

        assertThat(result.getCalificacionPromedio()).isZero();
        assertThat(result.getTotalValoraciones()).isZero();
    }

    @Test
    void obtenerReputacionVendedor_vendedorNoEncontrado_lanzaExcepcion() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtenerReputacionVendedor(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------ listarValoracionesProducto

    @Test
    void listarValoracionesProducto_devuelveSoloActivas() {
        Comprador comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = producto(10L, vendedor);

        Valoracion v = Valoracion.builder()
                .idVal(1L).comprador(comprador).vendedor(vendedor).producto(producto)
                .calificacion(4).estadoValo(true).build();

        when(valoracionRepository.findByProducto_IdPubAndEstadoValoTrue(10L)).thenReturn(List.of(v));

        List<ValoracionDto> result = service.listarValoracionesProducto(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCalificacion()).isEqualTo(4);
        assertThat(result.get(0).isEstadoValo()).isTrue();
    }

    // ------------------------------------------------------------------ inactivarValoracion

    @Test
    void inactivarValoracion_setEstadoFalseYActualizaReputacion() {
        Comprador comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = producto(10L, vendedor);

        Valoracion v = Valoracion.builder()
                .idVal(1L).comprador(comprador).vendedor(vendedor).producto(producto)
                .calificacion(4).estadoValo(true).build();

        when(valoracionRepository.findById(1L)).thenReturn(Optional.of(v));
        when(valoracionRepository.save(any())).thenReturn(v);
        when(valoracionRepository.calcularReputacionVendedor(2L)).thenReturn(3.0);
        when(userRepository.save(any())).thenReturn(vendedor);

        service.inactivarValoracion(1L);

        assertThat(v.isEstadoValo()).isFalse();
        verify(userRepository).save(vendedor); // reputación recalculada REQ-16
    }

    @Test
    void inactivarValoracion_noEncontrada_lanzaExcepcion() {
        when(valoracionRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.inactivarValoracion(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void inactivarValoracion_sinValoracionesRestantes_reputacionEnCero() {
        Comprador comprador = comprador(1L);
        Vendedor vendedor = vendedor(2L);
        Producto producto = producto(10L, vendedor);

        Valoracion v = Valoracion.builder()
                .idVal(1L).comprador(comprador).vendedor(vendedor).producto(producto)
                .calificacion(5).estadoValo(true).build();

        when(valoracionRepository.findById(1L)).thenReturn(Optional.of(v));
        when(valoracionRepository.save(any())).thenReturn(v);
        when(valoracionRepository.calcularReputacionVendedor(2L)).thenReturn(null); // sin valoraciones activas
        when(userRepository.save(any())).thenReturn(vendedor);

        service.inactivarValoracion(1L);

        assertThat(v.isEstadoValo()).isFalse();
        assertThat(vendedor.getCalificacion()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
