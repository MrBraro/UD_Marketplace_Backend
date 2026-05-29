/**
 * DTO de respuesta genérico para operaciones que solo retornan un mensaje de confirmación
 * en el marketplace UD.
 *
 * <p>Utilizado en endpoints como {@code POST /api/auth/logout} y en el controlador
 * de recursos protegidos donde no se retorna un recurso de negocio sino solo
 * una confirmación textual.
 *
 * @author
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResponse {

    /** Mensaje descriptivo de la operación realizada. */
    private String message;
}
