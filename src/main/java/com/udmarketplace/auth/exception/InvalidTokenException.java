/**
 * Excepción lanzada cuando el token JWT está ausente, es inválido, ha expirado
 * o fue previamente invalidado (logout) en el marketplace UD.
 *
 * <p>Mapeada a HTTP 401 por {@link GlobalExceptionHandler}.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.exception;

public class InvalidTokenException extends RuntimeException {

    /**
     * Construye la excepción con el mensaje de error proporcionado.
     *
     * @param message descripción del motivo de invalidez del token
     */
    public InvalidTokenException(String message) {
        super(message);
    }
}
