package com.udmarketplace.auth.exception;

/**
 * Excepción lanzada cuando se intenta autenticar con una cuenta bloqueada temporalmente.
 *
 * <p>Se activa cuando un usuario supera el número máximo de intentos fallidos
 * de autenticación configurado en {@code app.auth.max-intentos-fallidos} .
 *
 * <p>El {@code GlobalExceptionHandler} la captura y devuelve HTTP 423 (Locked)
 * con el mensaje que incluye la hora hasta la que la cuenta permanecerá bloqueada.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public class AccountBlockedException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje que describe el bloqueo.
     *
     * @param message mensaje descriptivo con la hora de desbloqueo
     */
    public AccountBlockedException(String message) {
        super(message);
    }
}
