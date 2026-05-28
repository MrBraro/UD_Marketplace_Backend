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
 * Manejador global de excepciones de la API REST del sistema UD Marketplace.
 *
 * <p>Intercepta todas las excepciones lanzadas desde cualquier controlador y las
 * transforma en respuestas HTTP estructuradas con el formato estándar {@link ErrorResponse},
 * garantizando respuestas consistentes en todos los endpoints.
 *
 * <p>Propiedades del manejo de errores:
 * <ul>
 *   <li>Código HTTP semánticamente correcto para cada tipo de error</li>
 *   <li>Cuerpo JSON consistente con status, mensaje y timestamp</li>
 *   <li>Sin exposición de stack traces ni información interna del servidor</li>
 *   <li>Mensajes de error en español orientados al usuario final</li>
 * </ul>
 *
 * <p>Mapa de excepciones a códigos HTTP:
 * <ul>
 *   <li>{@code 400} — Validación de campos ({@link org.springframework.web.bind.MethodArgumentNotValidException})</li>
 *   <li>{@code 401} — Credenciales inválidas, código 2FA o token JWT inválido</li>
 *   <li>{@code 403} — Acceso denegado por rol insuficiente</li>
 *   <li>{@code 404} — Recurso no encontrado ({@link RecursoNoEncontradoException})</li>
 *   <li>{@code 422} — Operación de negocio no permitida ({@link OperacionNoPermitidaException})</li>
 *   <li>{@code 423} — Cuenta bloqueada temporalmente ({@link AccountBlockedException})</li>
 *   <li>{@code 500} — Error interno no controlado</li>
 * </ul>
 *
 * @author
 * @version 1.0
 * @since 2026-05-28
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

    /** 423 — Cuenta bloqueada temporalmente por intentos fallidos (REQ-03). */
    @ExceptionHandler(AccountBlockedException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ErrorResponse handleAccountBlocked(AccountBlockedException ex) {
        return buildError(HttpStatus.LOCKED, ex.getMessage());
    }

    /** 404 — Recurso no encontrado. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** 422 — Operación de negocio no permitida. */
    @ExceptionHandler(OperacionNoPermitidaException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleOperacionNoPermitida(OperacionNoPermitidaException ex) {
        return buildError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /** 500 — Error interno no controlado. No expone detalles al cliente. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    /**
     * Construye un {@link ErrorResponse} estándar con el estado HTTP y el mensaje proporcionados.
     *
     * @param status  código HTTP del error
     * @param message mensaje descriptivo del error para el cliente
     * @return objeto {@code ErrorResponse} listo para serializar a JSON
     */
    private ErrorResponse buildError(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .status(status.value())
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
