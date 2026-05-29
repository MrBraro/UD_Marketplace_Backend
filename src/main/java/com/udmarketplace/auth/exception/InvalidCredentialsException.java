/**
 * Excepción lanzada cuando las credenciales de acceso son inválidas en el marketplace UD.
 *
 * <p>Se usa un mensaje genérico intencional para no revelar si el usuario existe o no,
 * previniendo ataques de enumeración de usuarios. Mapeada a HTTP 401 por
 * {@link GlobalExceptionHandler}.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    /**
     * Construye la excepción con el mensaje de error proporcionado.
     *
     * @param message mensaje descriptivo del error de autenticación
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
