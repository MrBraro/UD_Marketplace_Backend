package com.udmarketplace.transaccion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO de solicitud para registrar una intención de compra en el marketplace UD.
 * Solo accesible para usuarios con rol {@code COMPRADOR}.
 *
 * <p>El vendedor se asigna automáticamente del producto seleccionado.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Data
public class CrearTransaccionRequest {

    /** Identificador del producto que el comprador desea adquirir (obligatorio). */
    @NotNull(message = "El producto es obligatorio")
    private Long idPub;
}
