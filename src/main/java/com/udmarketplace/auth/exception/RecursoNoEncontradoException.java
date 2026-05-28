package com.udmarketplace.auth.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado en la base de datos.
 *
 * <p>Usada en todos los módulos del sistema cuando una entidad identificada
 * por su ID no existe o fue eliminada lógicamente (soft-delete).
 *
 * <p>El {@code GlobalExceptionHandler} la captura y devuelve HTTP 404 (Not Found).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje descriptivo del recurso no encontrado.
     *
     * @param message mensaje que indica qué recurso y con qué identificador no se encontró
     */
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
