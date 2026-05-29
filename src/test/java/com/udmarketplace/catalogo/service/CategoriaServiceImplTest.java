/**
 * Pruebas unitarias para CategoriaServiceImpl.
 * Cubre creación, listado, inactivación y manejo del contador de productos.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.catalogo.service;

import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.model.Administrador;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.catalogo.dto.CategoriaDto;
import com.udmarketplace.catalogo.dto.CrearCategoriaRequest;
import com.udmarketplace.catalogo.model.Categoria;
import com.udmarketplace.catalogo.repository.CategoriaRepository;
import com.udmarketplace.catalogo.service.impl.CategoriaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CategoriaServiceImpl service;

    private Administrador admin(Long id) {
        Administrador a = new Administrador();
        a.setCodigoUsua(id);
        a.setPrimerNombre("Admin");
        a.setPrimerApellido("Sistema");
        return a;
    }

    private Categoria categoria(Long id, boolean activo, int contador) {
        return Categoria.builder()
                .idCategoria(id)
                .nombreCat("Electrónica")
                .descripcionCat("Dispositivos")
                .activoCat(activo)
                .contadorProductos(contador)
                .build();
    }

    @Test
    void crearCategoria_exitoso() {
        Administrador admin = admin(1L);
        CrearCategoriaRequest request = new CrearCategoriaRequest();
        request.setNombreCat("Electrónica");
        request.setDescripcionCat("Dispositivos electrónicos");

        Categoria guardada = Categoria.builder()
                .idCategoria(10L)
                .nombreCat("Electrónica")
                .descripcionCat("Dispositivos electrónicos")
                .activoCat(true)
                .contadorProductos(0)
                .administrador(admin)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(categoriaRepository.save(any())).thenReturn(guardada);

        CategoriaDto result = service.crearCategoria(request, 1L);

        assertThat(result.getIdCategoria()).isEqualTo(10L);
        assertThat(result.getNombreCat()).isEqualTo("Electrónica");
        assertThat(result.isActivoCat()).isTrue();
        assertThat(result.getContadorProductos()).isZero();
    }

    @Test
    void crearCategoria_adminNoEncontrado_lanzaExcepcion() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        CrearCategoriaRequest request = new CrearCategoriaRequest();
        request.setNombreCat("Test");

        assertThatThrownBy(() -> service.crearCategoria(request, 99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void listarCategoriasActivas_devuelveDtosMapeados() {
        Categoria c1 = categoria(1L, true, 3);
        Categoria c2 = categoria(2L, true, 0);
        when(categoriaRepository.findByActivoCatTrue()).thenReturn(List.of(c1, c2));

        List<CategoriaDto> result = service.listarCategoriasActivas();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContadorProductos()).isEqualTo(3);
        assertThat(result.get(1).getContadorProductos()).isZero();
    }

    @Test
    void listarCategoriasActivas_listaVacia() {
        when(categoriaRepository.findByActivoCatTrue()).thenReturn(List.of());
        assertThat(service.listarCategoriasActivas()).isEmpty();
    }

    @Test
    void obtenerCategoria_encontrada() {
        Categoria c = categoria(5L, true, 2);
        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(c));

        CategoriaDto result = service.obtenerCategoria(5L);

        assertThat(result.getIdCategoria()).isEqualTo(5L);
        assertThat(result.getNombreCat()).isEqualTo("Electrónica");
    }

    @Test
    void obtenerCategoria_noEncontrada_lanzaExcepcion() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtenerCategoria(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void inactivarCategoria_setActivoCatFalse() {
        Categoria c = categoria(3L, true, 0);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(c));
        when(categoriaRepository.save(any())).thenReturn(c);

        service.inactivarCategoria(3L, 1L);

        assertThat(c.isActivoCat()).isFalse();
        verify(categoriaRepository).save(c);
    }

    @Test
    void inactivarCategoria_noEncontrada_lanzaExcepcion() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.inactivarCategoria(99L, 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void incrementarContador_sumaMasUno() {
        Categoria c = categoria(1L, true, 5);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(c));
        when(categoriaRepository.save(any())).thenReturn(c);

        service.incrementarContador(1L);

        assertThat(c.getContadorProductos()).isEqualTo(6);
        verify(categoriaRepository).save(c);
    }

    @Test
    void decrementarContador_restaUno() {
        Categoria c = categoria(1L, true, 3);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(c));
        when(categoriaRepository.save(any())).thenReturn(c);

        service.decrementarContador(1L);

        assertThat(c.getContadorProductos()).isEqualTo(2);
    }

    @Test
    void decrementarContador_enCero_noBajaDeNegativo() {
        Categoria c = categoria(1L, true, 0);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(c));
        when(categoriaRepository.save(any())).thenReturn(c);

        service.decrementarContador(1L);

        assertThat(c.getContadorProductos()).isZero();
    }
}
