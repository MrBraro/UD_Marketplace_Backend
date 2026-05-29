package com.udmarketplace.pqr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO de respuesta con los datos completos de una PQR del marketplace UD.
 *
 * <p>Incluye la identificación del usuario creador, el administrador asignado,
 * la fecha/hora de creación, el estado actual y el historial de interacciones
 * ordenado cronológicamente.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PqrDto {

    /** Número de radicado único de la PQR (REQ-10). */
    private Long radicado;

    /** Identificador del usuario que creó la PQR. */
    private Long codigoUsuario;

    /** Nombre completo del usuario creador. */
    private String nombreUsuario;

    /** Tipo de PQR: PETICION, QUEJA o RECLAMO. */
    private String tipoPqr;

    /** Descripción detallada de la solicitud. */
    private String descripcionPqr;

    /** Estado actual de la PQR: ENVIADA, EN_PROCESO o CERRADA. */
    private String estadoPqr;

    /** Fecha de creación de la PQR (REQ-11). */
    private LocalDate fechaCreacionPqr;

    /** Hora de creación de la PQR (REQ-11). */
    private LocalTime horaCreacionPqr;

    /** Identificador del administrador asignado automáticamente (REQ-13). */
    private Long codigoAdmin;

    /** Historial de interacciones/mensajes ordenado cronológicamente (REQ-14). */
    private List<InteraccionDto> interacciones;
}
