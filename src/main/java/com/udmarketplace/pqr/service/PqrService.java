package com.udmarketplace.pqr.service;

import com.udmarketplace.pqr.dto.AgregarInteraccionRequest;
import com.udmarketplace.pqr.dto.CrearPqrRequest;
import com.udmarketplace.pqr.dto.InteraccionDto;
import com.udmarketplace.pqr.dto.PqrDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Contrato del servicio de gestión de PQRs (Peticiones, Quejas y Reclamos) del marketplace UD.
 *
 * <p>Gestiona el ciclo de vida completo de una PQR con los siguientes comportamientos automáticos:
 * <ul>
 *   <li> radicado único asignado por AUTO_INCREMENT</li>
 *   <li> fecha y hora de creación registradas automáticamente</li>
 *   <li> archivo adjunto validado (tipo MIME, extensión, máx. 5 MB) y almacenado como BLOB</li>
 *   <li> administrador asignado automáticamente al de menor carga de PQRs abiertas</li>
 *   <li> historial de interacciones con autor, mensaje y fecha/hora</li>
 * </ul>
 *
 * <p>Acceso restringido: solo el usuario creador y el administrador asignado pueden ver una PQR.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface PqrService {

    /**
     * Crea una nueva PQR con radicado único, registra fecha/hora de creación,
     * almacena el adjunto si se provee  y asigna el administrador con menor carga .
     *
     * @param request        datos de la PQR (tipo y descripción)
     * @param adjunto        archivo adjunto opcional (imagen o PDF, máx. 5 MB)
     * @param codigoUsuario  identificador del usuario que crea la PQR
     * @return DTO de la PQR creada con su número de radicado
     * @throws com.udmarketplace.auth.exception.OperacionNoPermitidaException si el adjunto no cumple los requisitos
     */
    PqrDto crearPqr(CrearPqrRequest request, MultipartFile adjunto, Long codigoUsuario);

    /**
     * Retorna el detalle de una PQR. Solo accesible para el usuario creador o el administrador asignado.
     *
     * @param radicado      número de radicado de la PQR
     * @param codigoUsuario identificador del usuario que solicita la consulta
     * @return DTO completo de la PQR con sus interacciones
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException    si la PQR no existe
     * @throws com.udmarketplace.auth.exception.OperacionNoPermitidaException si no tiene acceso
     */
    PqrDto obtenerPqr(Long radicado, Long codigoUsuario);

    /**
     * Lista todas las PQRs creadas por el usuario identificado.
     *
     * @param codigoUsuario identificador del usuario
     * @return lista de PQRs del usuario
     */
    List<PqrDto> listarPqrsUsuario(Long codigoUsuario);

    /**
     * Agrega un mensaje/interacción a una PQR existente.
     * No se permiten interacciones en PQRs con estado {@code CERRADA}.
     *
     * @param radicado   número de radicado de la PQR
     * @param request    DTO con el mensaje de la interacción
     * @param codigoAutor identificador del usuario que envía el mensaje
     * @return DTO de la interacción registrada
     * @throws com.udmarketplace.auth.exception.OperacionNoPermitidaException si la PQR está cerrada
     */
    InteraccionDto agregarInteraccion(Long radicado, AgregarInteraccionRequest request, Long codigoAutor);

    /**
     * Cambia el estado de una PQR a uno de los valores permitidos: ENVIADA, EN_PROCESO o CERRADA.
     * Solo accesible para administradores.
     *
     * @param radicado    número de radicado de la PQR
     * @param nuevoEstado nuevo estado (ENVIADA, EN_PROCESO, CERRADA)
     * @param codigoAdmin identificador del administrador que realiza el cambio
     * @return DTO de la PQR con el estado actualizado
     * @throws com.udmarketplace.auth.exception.OperacionNoPermitidaException si el estado es inválido
     */
    PqrDto cambiarEstado(Long radicado, String nuevoEstado, Long codigoAdmin);

    /**
     * Cierra definitivamente una PQR. Solo accesible para administradores.
     * Una PQR cerrada no acepta nuevas interacciones.
     *
     * @param radicado    número de radicado de la PQR a cerrar
     * @param codigoAdmin identificador del administrador que cierra la PQR
     * @return DTO de la PQR con estado CERRADA
     */
    PqrDto cerrarPqr(Long radicado, Long codigoAdmin);
}
