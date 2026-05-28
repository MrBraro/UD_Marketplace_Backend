package com.udmarketplace.pqr.service.impl;

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
import com.udmarketplace.pqr.service.PqrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

/**
 * Implementación del servicio de gestión de PQRs del marketplace UD.
 *
 * <p>Implementa automáticamente al crear una PQR:
 * <ul>
 *   <li> radicado único por AUTO_INCREMENT de MySQL</li>
 *   <li> fecha ({@code LocalDate.now()}) y hora ({@code LocalTime.now()}) de creación</li>
 *   <li> validación y almacenamiento del adjunto como BLOB (máx. 5 MB, imagen/PDF)</li>
 *   <li> asignación al administrador con menos PQRs abiertas; si no hay PQRs previas,
 *       busca cualquier administrador activo</li>
 *   <li> registro de interacciones con autor, mensaje y fecha/hora</li>
 * </ul>
 *
 * <p>Todas las operaciones de escritura son transaccionales.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Service
@RequiredArgsConstructor
public class PqrServiceImpl implements PqrService {

    /** Repositorio de PQRs para operaciones CRUD y consultas de carga de admins. */
    private final PqrRepository pqrRepository;

    /** Repositorio de interacciones para el historial cronológico de mensajes. */
    private final InteraccionPqrRepository interaccionRepo;

    /** Repositorio de usuarios para obtener el creador de la PQR y los administradores. */
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Asigna automáticamente el radicado (BD), la fecha/hora de creación y el administrador
     * con menor carga de PQRs abiertas. Valida el adjunto antes de persistirlo.
     */
    @Override
    @Transactional
    public PqrDto crearPqr(CrearPqrRequest request, MultipartFile adjunto, Long codigoUsuario) {
        User usuario = userRepository.findById(codigoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Pqr pqr = Pqr.builder()
                .usuario(usuario)
                .tipoPqr(request.getTipoPqr())
                .descripcionPqr(request.getDescripcionPqr())
                .estadoPqr(EstadoPqr.ENVIADA.name())
                // REQ-11: registrar fecha y hora de creación
                .fechaCreacionPqr(LocalDate.now())
                .horaCreacionPqr(LocalTime.now())
                // REQ-13: asignar al admin con menor carga
                .administrador(asignarAdmin())
                .build();

        //  almacenar adjunto
        if (adjunto != null && !adjunto.isEmpty()) {
            validarAdjunto(adjunto);
            try {
                pqr.setImagenPqr(adjunto.getBytes());
            } catch (IOException e) {
                throw new OperacionNoPermitidaException("Error al procesar el adjunto");
            }
        }

        return toDto(pqrRepository.save(pqr));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Valida que el solicitante sea el creador de la PQR o el administrador asignado.
     */
    @Override
    public PqrDto obtenerPqr(Long radicado, Long codigoUsuario) {
        Pqr pqr = pqrRepository.findById(radicado)
                .orElseThrow(() -> new RecursoNoEncontradoException("PQR no encontrada: " + radicado));
        // REQ: restringir acceso al creador y admins
        boolean esAdmin = pqr.getAdministrador() != null
                && pqr.getAdministrador().getCodigoUsua().equals(codigoUsuario);
        boolean esCreador = pqr.getUsuario().getCodigoUsua().equals(codigoUsuario);
        if (!esCreador && !esAdmin) {
            throw new OperacionNoPermitidaException("No tiene acceso a esta PQR");
        }
        return toDto(pqr);
    }

    /** {@inheritDoc} */
    @Override
    public List<PqrDto> listarPqrsUsuario(Long codigoUsuario) {
        return pqrRepository.findByUsuario_CodigoUsua(codigoUsuario)
                .stream().map(this::toDto).toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Bloquea agregar interacciones a PQRs cerradas para preservar la integridad del historial.
     */
    @Override
    @Transactional
    public InteraccionDto agregarInteraccion(Long radicado, AgregarInteraccionRequest request, Long codigoAutor) {
        Pqr pqr = pqrRepository.findById(radicado)
                .orElseThrow(() -> new RecursoNoEncontradoException("PQR no encontrada: " + radicado));

        if (EstadoPqr.CERRADA.name().equals(pqr.getEstadoPqr())) {
            throw new OperacionNoPermitidaException("No se pueden agregar interacciones a una PQR cerrada");
        }

        User autor = userRepository.findById(codigoAutor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        InteraccionPqr interaccion = InteraccionPqr.builder()
                .pqr(pqr)
                .autor(autor)
                .mensaje(request.getMensaje())
                .fechaHora(LocalDateTime.now())
                .build();

        interaccionRepo.save(interaccion);
        return toInteraccionDto(interaccion);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public PqrDto cambiarEstado(Long radicado, String nuevoEstado, Long codigoAdmin) {
        Pqr pqr = pqrRepository.findById(radicado)
                .orElseThrow(() -> new RecursoNoEncontradoException("PQR no encontrada: " + radicado));
        try {
            EstadoPqr.valueOf(nuevoEstado);
        } catch (IllegalArgumentException e) {
            throw new OperacionNoPermitidaException("Estado inválido: " + nuevoEstado + ". Valores: ENVIADA, EN_PROCESO, CERRADA");
        }
        pqr.setEstadoPqr(nuevoEstado);
        return toDto(pqrRepository.save(pqr));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public PqrDto cerrarPqr(Long radicado, Long codigoAdmin) {
        return cambiarEstado(radicado, EstadoPqr.CERRADA.name(), codigoAdmin);
    }


    /**
     * Selecciona el administrador con menor cantidad de PQRs abiertas (REQ-13).
     * Si no hay PQRs previas, busca cualquier administrador activo en el sistema.
     * Si no hay administradores disponibles retorna {@code null}.
     *
     * @return el administrador con menor carga de PQRs, o {@code null} si no hay admins
     */
    private Administrador asignarAdmin() {
        List<Object[]> carga = pqrRepository.contarPqrsAbiertas();
        Long codigoAdminMenorCarga = null;

        if (!carga.isEmpty()) {
            codigoAdminMenorCarga = (Long) carga.get(0)[0];
        } else {
            // Si no hay PQRs previas, buscar cualquier administrador activo
            codigoAdminMenorCarga = userRepository.findAll().stream()
                    .filter(u -> u instanceof Administrador && u.isActivo())
                    .map(User::getCodigoUsua)
                    .findFirst()
                    .orElse(null);
        }

        if (codigoAdminMenorCarga == null) return null;

        Long finalCodigoAdmin = codigoAdminMenorCarga;
        return (Administrador) userRepository.findById(finalCodigoAdmin).orElse(null);
    }

    /**
     * Valida que el archivo adjunto cumpla los requisitos de tipo y tamaño (REQ-12).
     * Acepta imágenes (cualquier tipo MIME que empiece con {@code image/}) y PDFs.
     * El tamaño máximo permitido es 5 MB.
     *
     * @param adjunto archivo adjunto a validar
     * @throws com.udmarketplace.auth.exception.OperacionNoPermitidaException si supera el tamaño o es de tipo no permitido
     */
    private void validarAdjunto(MultipartFile adjunto) {
        long maxBytes = 5 * 1024 * 1024L; // 5 MB
        if (adjunto.getSize() > maxBytes) {
            throw new OperacionNoPermitidaException("El adjunto no puede superar 5 MB");
        }
        String contentType = adjunto.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new OperacionNoPermitidaException("Solo se aceptan imágenes y PDF como adjuntos");
        }
    }

 
    /**
     * Convierte una entidad {@link Pqr} a su DTO de respuesta incluyendo las interacciones
     * ordenadas cronológicamente.
     *
     * @param pqr entidad de la PQR
     * @return DTO con todos los datos de la PQR y su historial de interacciones
     */
    private PqrDto toDto(Pqr pqr) {
        List<InteraccionDto> interacciones = interaccionRepo
                .findByPqr_RadicadoOrderByFechaHoraAsc(pqr.getRadicado())
                .stream().map(this::toInteraccionDto).toList();

        return PqrDto.builder()
                .radicado(pqr.getRadicado())
                .codigoUsuario(pqr.getUsuario().getCodigoUsua())
                .nombreUsuario(pqr.getUsuario().getPrimerNombre() + " " + pqr.getUsuario().getPrimerApellido())
                .tipoPqr(pqr.getTipoPqr())
                .descripcionPqr(pqr.getDescripcionPqr())
                .estadoPqr(pqr.getEstadoPqr())
                .fechaCreacionPqr(pqr.getFechaCreacionPqr())
                .horaCreacionPqr(pqr.getHoraCreacionPqr())
                .codigoAdmin(pqr.getAdministrador() != null ? pqr.getAdministrador().getCodigoUsua() : null)
                .interacciones(interacciones)
                .build();
    }

    /**
     * Convierte una entidad {@link InteraccionPqr} a su DTO de respuesta.
     *
     * @param i entidad de la interacción
     * @return DTO de la interacción
     */
    private InteraccionDto toInteraccionDto(InteraccionPqr i) {
        return InteraccionDto.builder()
                .idInteraccion(i.getIdInteraccion())
                .codigoAutor(i.getAutor().getCodigoUsua())
                .nombreAutor(i.getAutor().getPrimerNombre() + " " + i.getAutor().getPrimerApellido())
                .mensaje(i.getMensaje())
                .fechaHora(i.getFechaHora())
                .build();
    }
}
