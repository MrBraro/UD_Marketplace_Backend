package com.udmarketplace.auth.exception;

/**
 * Excepción lanzada cuando el código 2FA recibido es inválido o no coincide (RF11).
 */
public class TwoFactorException extends RuntimeException {

    public TwoFactorException(String message) {
        super(message);
    }
}
