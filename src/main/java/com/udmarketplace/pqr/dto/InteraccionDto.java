package com.udmarketplace.pqr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con los datos de una interacción/mensaje dentro de una PQR (REQ-14).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteraccionDto {

    /** Identificador único de la interacción. */
    private Long idInteraccion;

    /** Identificador del usuario que envió el mensaje. */
    private Long codigoAutor;

    /** Nombre completo del autor del mensaje. */
    private String nombreAutor;

    /** Contenido del mensaje de la interacción. */
    private String mensaje;

    /** Fecha y hora en que se registró la interacción. */
    private LocalDateTime fechaHora;
}
