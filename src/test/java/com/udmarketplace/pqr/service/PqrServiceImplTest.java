/**
 * Pruebas unitarias para PqrServiceImpl.
 * Cubre creación, consulta, interacciones, cambio de estado y validación de adjuntos.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.pqr.service;

import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.model.Administrador;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.pqr.dto.AgregarInteraccionRequest;
import com.udmarketplace.pqr.dto.CrearPqrRequest;
import com.udmarketplace.pqr.dto.InteraccionDto;
import com.udmarketplace.pqr.dto.PqrDto;
import com.udmarketplace.pqr.model.EstadoPqr;
import com.udmarketplace.pqr.model.InteraccionPqr;
import com.udmarketplace.pqr.model.Pqr;
import com.udmarketplace.pqr.repository.InteraccionPqrRepository;
import com.udmarketplace.pqr.repository.PqrRepository;
import com.udmarketplace.pqr.service.impl.PqrServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PqrServiceImplTest {

    @Mock
    private PqrRepository pqrRepository;
    @Mock
    private InteraccionPqrRepository interaccionRepo;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private PqrServiceImpl service;

    private User usuario(Long id) {
        User u = new User();
        u.setCodigoUsua(id);
        u.setPrimerNombre("Carlos");
        u.setPrimerApellido("García");
        return u;
    }

    private Administrador admin(Long id) {
        Administrador a = new Administrador();
        a.setCodigoUsua(id);
        a.setPrimerNombre("Admin");
        a.setPrimerApellido("Sistema");
        a.setActivo(true);
        return a;
    }

    private Pqr pqrConEstado(Long radicado, User usuario, Administrador administrador, String estado) {
        return Pqr.builder()
                .radicado(radicado)
                .usuario(usuario)
                .tipoPqr("QUEJA")
                .descripcionPqr("Problema con el producto")
                .estadoPqr(estado)
                .fechaCreacionPqr(LocalDate.now())
                .horaCreacionPqr(LocalTime.now())
                .administrador(administrador)
                .build();
    }

    private CrearPqrRequest requestPqr(String tipo) {
        CrearPqrRequest r = new CrearPqrRequest();
        r.setTipoPqr(tipo);
        r.setDescripcionPqr("Descripción de prueba");
        return r;
    }

    // ------------------------------------------------------------------ crearPqr

    @Test
    void crearPqr_sinAdjunto_exitoso() {
        User u = usuario(1L);
        Administrador a = admin(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(pqrRepository.contarPqrsAbiertas()).thenReturn(List.of(new Object[]{2L, 1L}));
        when(userRepository.findById(2L)).thenReturn(Optional.of(a));

        Pqr guardada = pqrConEstado(100L, u, a, EstadoPqr.ENVIADA.name());
        when(pqrRepository.save(any())).thenReturn(guardada);
        when(interaccionRepo.findByPqr_RadicadoOrderByFechaHoraAsc(100L)).thenReturn(List.of());

        PqrDto result = service.crearPqr(requestPqr("QUEJA"), null, 1L);

        assertThat(result.getRadicado()).isEqualTo(100L);
        assertThat(result.getTipoPqr()).isEqualTo("QUEJA");
        assertThat(result.getEstadoPqr()).isEqualTo(EstadoPqr.ENVIADA.name());
        assertThat(result.getCodigoAdmin()).isEqualTo(2L);
    }

    @Test
    void crearPqr_usuarioNoEncontrado_lanzaExcepcion() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.crearPqr(requestPqr("PETICION"), null, 99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(pqrRepository, never()).save(any());
    }

    @Test
    void crearPqr_adjuntoExcedeTamano_lanzaExcepcion() {
        User u = usuario(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(pqrRepository.contarPqrsAbiertas()).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of());

        byte[] datos = new byte[6 * 1024 * 1024]; // 6 MB > 5 MB límite
        MockMultipartFile adjunto = new MockMultipartFile("adjunto", "grande.jpg", "image/jpeg", datos);

        assertThatThrownBy(() -> service.crearPqr(requestPqr("QUEJA"), adjunto, 1L))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("5 MB");
    }

    @Test
    void crearPqr_adjuntoTipoNoPermitido_lanzaExcepcion() {
        User u = usuario(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(pqrRepository.contarPqrsAbiertas()).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of());

        MockMultipartFile adjunto = new MockMultipartFile(
                "adjunto", "script.exe", "application/x-msdownload", "datos".getBytes());

        assertThatThrownBy(() -> service.crearPqr(requestPqr("QUEJA"), adjunto, 1L))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("PDF");
    }

    @Test
    void crearPqr_adjuntoPdfValido_exitoso() {
        User u = usuario(1L);
        Administrador a = admin(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(pqrRepository.contarPqrsAbiertas()).thenReturn(List.of(new Object[]{2L, 0L}));
        when(userRepository.findById(2L)).thenReturn(Optional.of(a));

        byte[] datos = "contenido pdf".getBytes();
        MockMultipartFile adjunto = new MockMultipartFile(
                "adjunto", "doc.pdf", "application/pdf", datos);

        Pqr guardada = pqrConEstado(101L, u, a, EstadoPqr.ENVIADA.name());
        when(pqrRepository.save(any())).thenReturn(guardada);
        when(interaccionRepo.findByPqr_RadicadoOrderByFechaHoraAsc(101L)).thenReturn(List.of());

        PqrDto result = service.crearPqr(requestPqr("RECLAMO"), adjunto, 1L);

        assertThat(result.getRadicado()).isEqualTo(101L);
    }

    // ------------------------------------------------------------------ obtenerPqr

    @Test
    void obtenerPqr_creadorPuedeAcceder() {
        User u = usuario(1L);
        Administrador a = admin(2L);
        Pqr pqr = pqrConEstado(50L, u, a, EstadoPqr.ENVIADA.name());

        when(pqrRepository.findById(50L)).thenReturn(Optional.of(pqr));
        when(interaccionRepo.findByPqr_RadicadoOrderByFechaHoraAsc(50L)).thenReturn(List.of());

        PqrDto result = service.obtenerPqr(50L, 1L);

        assertThat(result.getRadicado()).isEqualTo(50L);
    }

    @Test
    void obtenerPqr_adminAsignadoPuedeAcceder() {
        User u = usuario(1L);
        Administrador a = admin(2L);
        Pqr pqr = pqrConEstado(50L, u, a, EstadoPqr.EN_PROCESO.name());

        when(pqrRepository.findById(50L)).thenReturn(Optional.of(pqr));
        when(interaccionRepo.findByPqr_RadicadoOrderByFechaHoraAsc(50L)).thenReturn(List.of());

        PqrDto result = service.obtenerPqr(50L, 2L);

        assertThat(result.getEstadoPqr()).isEqualTo(EstadoPqr.EN_PROCESO.name());
    }

    @Test
    void obtenerPqr_terceroNoAutorizado_lanzaExcepcion() {
        User u = usuario(1L);
        Administrador a = admin(2L);
        Pqr pqr = pqrConEstado(50L, u, a, EstadoPqr.ENVIADA.name());

        when(pqrRepository.findById(50L)).thenReturn(Optional.of(pqr));

        assertThatThrownBy(() -> service.obtenerPqr(50L, 99L))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    void obtenerPqr_radicadoNoExiste_lanzaExcepcion() {
        when(pqrRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtenerPqr(99L, 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------ listarPqrsUsuario

    @Test
    void listarPqrsUsuario_devuelveLista() {
        User u = usuario(1L);
        Administrador a = admin(2L);
        Pqr p1 = pqrConEstado(1L, u, a, EstadoPqr.ENVIADA.name());
        Pqr p2 = pqrConEstado(2L, u, a, EstadoPqr.CERRADA.name());

        when(pqrRepository.findByUsuario_CodigoUsua(1L)).thenReturn(List.of(p1, p2));
        when(interaccionRepo.findByPqr_RadicadoOrderByFechaHoraAsc(anyLong())).thenReturn(List.of());

        List<PqrDto> result = service.listarPqrsUsuario(1L);

        assertThat(result).hasSize(2);
    }

    // ------------------------------------------------------------------ agregarInteraccion

    @Test
    void agregarInteraccion_exitosa() {
        User u = usuario(1L);
        Administrador a = admin(2L);
        Pqr pqr = pqrConEstado(50L, u, a, EstadoPqr.EN_PROCESO.name());

        when(pqrRepository.findById(50L)).thenReturn(Optional.of(pqr));
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        InteraccionPqr interaccion = InteraccionPqr.builder()
                .idInteraccion(1L).pqr(pqr).autor(u)
                .mensaje("Necesito seguimiento").fechaHora(LocalDateTime.now()).build();
        when(interaccionRepo.save(any())).thenReturn(interaccion);

        AgregarInteraccionRequest request = new AgregarInteraccionRequest();
        request.setMensaje("Necesito seguimiento");

        InteraccionDto result = service.agregarInteraccion(50L, request, 1L);

        assertThat(result.getMensaje()).isEqualTo("Necesito seguimiento");
        assertThat(result.getCodigoAutor()).isEqualTo(1L);
    }

    @Test
    void agregarInteraccion_pqrCerrada_lanzaExcepcion() {
        User u = usuario(1L);
        Administrador a = admin(2L);
        Pqr pqr = pqrConEstado(50L, u, a, EstadoPqr.CERRADA.name());

        when(pqrRepository.findById(50L)).thenReturn(Optional.of(pqr));

        AgregarInteraccionRequest request = new AgregarInteraccionRequest();
        request.setMensaje("Intento de mensaje");

        assertThatThrownBy(() -> service.agregarInteraccion(50L, request, 1L))
                .isInstanceOf(OperacionNoPermitidaException.class);
        verify(interaccionRepo, never()).save(any());
    }

    @Test
    void agregarInteraccion_pqrNoExiste_lanzaExcepcion() {
        when(pqrRepository.findById(anyLong())).thenReturn(Optional.empty());
        AgregarInteraccionRequest request = new AgregarInteraccionRequest();
        request.setMensaje("Mensaje");
        assertThatThrownBy(() -> service.agregarInteraccion(99L, request, 1L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------ cambiarEstado

    @Test
    void cambiarEstado_aEnProceso_exitoso() {
        User u = usuario(1L);
        Administrador a = admin(2L);
        Pqr pqr = pqrConEstado(50L, u, a, EstadoPqr.ENVIADA.name());

        when(pqrRepository.findById(50L)).thenReturn(Optional.of(pqr));
        when(pqrRepository.save(any())).thenReturn(pqr);
        when(interaccionRepo.findByPqr_RadicadoOrderByFechaHoraAsc(50L)).thenReturn(List.of());

        PqrDto result = service.cambiarEstado(50L, "EN_PROCESO", 2L);

        assertThat(result.getEstadoPqr()).isEqualTo("EN_PROCESO");
    }

    @Test
    void cambiarEstado_estadoInvalido_lanzaExcepcion() {
        User u = usuario(1L);
        Administrador a = admin(2L);
        Pqr pqr = pqrConEstado(50L, u, a, EstadoPqr.ENVIADA.name());

        when(pqrRepository.findById(50L)).thenReturn(Optional.of(pqr));

        assertThatThrownBy(() -> service.cambiarEstado(50L, "ESTADO_INVALIDO", 2L))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("CERRADA");
    }

    // ------------------------------------------------------------------ cerrarPqr

    @Test
    void cerrarPqr_setEstadoCerrada() {
        User u = usuario(1L);
        Administrador a = admin(2L);
        Pqr pqr = pqrConEstado(50L, u, a, EstadoPqr.EN_PROCESO.name());

        when(pqrRepository.findById(50L)).thenReturn(Optional.of(pqr));
        when(pqrRepository.save(any())).thenReturn(pqr);
        when(interaccionRepo.findByPqr_RadicadoOrderByFechaHoraAsc(50L)).thenReturn(List.of());

        PqrDto result = service.cerrarPqr(50L, 2L);

        assertThat(result.getEstadoPqr()).isEqualTo(EstadoPqr.CERRADA.name());
        assertThat(pqr.getEstadoPqr()).isEqualTo(EstadoPqr.CERRADA.name());
    }
}
