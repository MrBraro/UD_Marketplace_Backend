package com.udmarketplace.auth.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación de negocio no permitida.
 *
 * <p>Se usa para reglas de negocio que no son errores de autenticación ni de autorización,
 * sino restricciones propias del dominio. Por ejemplo:
 * <ul>
 *   <li>Confirmar una transacción que ya fue confirmada</li>
 *   <li>Valorar un producto que no fue comprado</li>
 *   <li>Agregar interacciones a una PQR cerrada</li>
 *   <li>Modificar un producto que no pertenece al vendedor</li>
 * </ul>
 *
 * <p>El {@code GlobalExceptionHandler} la captura y devuelve HTTP 422 (Unprocessable Entity).
 *
 * @version 1.0
 * @since 2026-05-28
 */
public class OperacionNoPermitidaException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje que describe la regla de negocio violada.
     *
     * @param message descripción de por qué la operación no está permitida
     */
    public OperacionNoPermitidaException(String message) {
        super(message);
    }
}
