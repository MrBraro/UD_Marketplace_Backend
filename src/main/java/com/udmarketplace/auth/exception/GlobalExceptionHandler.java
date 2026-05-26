package com.udmarketplace.auth.exception;

import com.udmarketplace.auth.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones de la API.
 *
 * <p>Intercepta excepciones lanzadas desde cualquier controlador y las convierte
 * en respuestas HTTP con el formato estándar {@link ErrorResponse}.
 *
 * <p>Garantiza que todos los errores tengan:
 * <ul>
 *   <li>Código HTTP correcto</li>
 *   <li>Body JSON consistente</li>
 *   <li>Sin stack traces expuestos al cliente</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 401 — Credenciales de acceso inválidas (RF08). */
    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** 401 — Código 2FA inválido (RF11). */
    @ExceptionHandler(TwoFactorException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleTwoFactor(TwoFactorException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** 401 — Token JWT ausente o inválido (RF13). */
    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidToken(InvalidTokenException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** 403 — Acceso denegado por rol insuficiente (RF24). */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "Acceso denegado: permisos insuficientes para este recurso");
    }

    /** 400 — Validación de campos del request body (@Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    /** 500 — Error interno no controlado. No expone detalles al cliente. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ErrorResponse buildError(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .status(status.value())
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
