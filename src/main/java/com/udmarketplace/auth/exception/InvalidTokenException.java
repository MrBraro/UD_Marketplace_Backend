package com.udmarketplace.auth.exception;

/**
 * Excepción lanzada cuando el token JWT no está presente, es inválido
 * o ya fue invalidado (RF13).
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
