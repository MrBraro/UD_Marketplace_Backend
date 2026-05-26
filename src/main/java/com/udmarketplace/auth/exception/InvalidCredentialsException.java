package com.udmarketplace.auth.exception;

/**
 * Excepción lanzada cuando las credenciales de acceso son inválidas (RF08).
 *
 * <p>Se usa un mensaje genérico intencional ("Invalid username or password")
 * para no revelar si el usuario existe o no (previene user enumeration attacks).
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
