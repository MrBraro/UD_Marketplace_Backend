/**
 * Excepción lanzada cuando el código 2FA recibido es inválido, no coincide
 * o ha expirado en el marketplace UD.
 *
 * <p>Mapeada a HTTP 401 por {@link GlobalExceptionHandler}.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.exception;

public class TwoFactorException extends RuntimeException {

    /**
     * Construye la excepción con el mensaje de error proporcionado.
     *
     * @param message descripción del motivo de fallo en la verificación 2FA
     */
    public TwoFactorException(String message) {
        super(message);
    }
}
