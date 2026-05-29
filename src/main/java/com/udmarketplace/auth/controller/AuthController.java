package com.udmarketplace.auth.controller;

import com.udmarketplace.auth.dto.LoginRequest;
import com.udmarketplace.auth.dto.LoginResponse;
import com.udmarketplace.auth.dto.LoginStepResponse;
import com.udmarketplace.auth.dto.MessageResponse;
import com.udmarketplace.auth.dto.RecuperarPasswordRequest;
import com.udmarketplace.auth.dto.ResetPasswordRequest;
import com.udmarketplace.auth.dto.TwoFactorRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.exception.InvalidTokenException;
import com.udmarketplace.auth.service.AuthService;
import com.udmarketplace.auth.service.RecuperacionPasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación y gestión de sesión del sistema UD Marketplace.
 *
 * <p>Expone todos los endpoints del flujo de autenticación en dos factores (2FA)
 * y recuperación de contraseña:
 * <ul>
 *   <li>{@code POST /api/auth/login}               — Paso 1: valida credenciales y envía código 2FA</li>
 *   <li>{@code POST /api/auth/verifyTwoFactor}      — Paso 2: valida código 2FA y emite JWT</li>
 *   <li>{@code POST /api/auth/logout}               — Invalida el token de sesión activo</li>
 *   <li>{@code GET  /api/auth/me}                   — Retorna información del usuario autenticado</li>
 *   <li>{@code POST /api/auth/recuperar-password}   — Solicita token de recuperación de contraseña</li>
 *   <li>{@code POST /api/auth/reset-password}       — Establece nueva contraseña con token válido</li>
 * </ul>
 *
 * @version 1.0
 * @since 2026-05-28
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** Servicio principal de autenticación que orquesta el flujo 2FA. */
    private final AuthService authService;

    /** Servicio de recuperación de contraseña con tokens únicos. */
    private final RecuperacionPasswordService recuperacionPasswordService;

    /**
     * Paso 1 del login: valida las credenciales del usuario y, si son correctas,
     * genera y envía el código 2FA al correo registrado.
     *
     * <p>La dirección IP se extrae automáticamente del request, considerando
     * proxies mediante el header {@code X-Forwarded-For}.
     *
     * @param request     DTO con correoUsuario y passwordUsua
     * @param httpRequest request HTTP original para extracción de la IP de origen
     * @return respuesta indicando que se requiere verificación 2FA
     */
    @PostMapping("/login")
    public ResponseEntity<LoginStepResponse> login(@Valid @RequestBody LoginRequest request,
                                                   HttpServletRequest httpRequest) {
        String ip = obtenerIp(httpRequest);
        LoginStepResponse response = authService.login(request, ip);
        return ResponseEntity.ok(response);
    }

    /**
     * Inicia el flujo de recuperación de contraseña.
     * Genera un token UUID único y solicita al backend Python el envío del correo.
     *
     * <p>Siempre responde HTTP 200 independientemente de si el correo existe,
     * para no revelar qué correos están registrados en el sistema.
     *
     * @param request DTO con el correo electrónico del usuario
     * @return mensaje genérico de confirmación
     */
    @PostMapping("/recuperar-password")
    public ResponseEntity<MessageResponse> recuperarPassword(
            @Valid @RequestBody RecuperarPasswordRequest request) {
        recuperacionPasswordService.solicitarRecuperacion(request.getCorreoUsuario());
        return ResponseEntity.ok(new MessageResponse(
                "Si el correo está registrado, recibirás las instrucciones de recuperación"));
    }

    /**
     * Establece una nueva contraseña usando el token de recuperación recibido por correo.
     * El token debe ser válido, no haber sido usado y no estar expirado.
     *
     * @param request DTO con el token de recuperación y la nueva contraseña
     * @return mensaje de confirmación de actualización exitosa
     * @throws com.udmarketplace.auth.exception.InvalidTokenException si el token es inválido o expiró
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        recuperacionPasswordService.resetearPassword(request.getToken(), request.getNuevaPassword());
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada exitosamente"));
    }

    /**
     * Extrae la dirección IP de origen del request HTTP.
     * Considera el header {@code X-Forwarded-For} para soportar proxies y balanceadores de carga.
     *
     * @param request request HTTP del cliente
     * @return dirección IP del cliente de origen
     */
    private String obtenerIp(HttpServletRequest request) {
        String xfwdFor = request.getHeader("X-Forwarded-For");
        return (xfwdFor != null && !xfwdFor.isEmpty()) ? xfwdFor.split(",")[0].trim()
                : request.getRemoteAddr();
    }

    /**
     * Paso 2 del login: valida el código 2FA recibido por correo y emite el JWT de sesión.
     *
     * <p>El JWT incluye el {@code userId} del usuario para asociar el token con su
     * propietario (REQ-01). Tras una verificación exitosa, el código 2FA queda invalidado.
     *
     * @param request DTO con correoUsuario y el código 2FA de 6 dígitos
     * @return JWT de sesión con datos básicos del usuario autenticado
     */
    @PostMapping("/verifyTwoFactor")
    public ResponseEntity<LoginResponse> verifyTwoFactor(@Valid @RequestBody TwoFactorRequest request) {
        LoginResponse response = authService.verifyTwoFactor(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cierra la sesión del usuario autenticado invalidando el token activo en la blacklist.
     *
     * <p>El token extraído del header {@code Authorization: Bearer <token>} queda
     * inutilizable para futuras solicitudes aunque no haya expirado.
     *
     * @param httpRequest request HTTP del que se extrae el token Bearer
     * @return mensaje de confirmación de cierre de sesión
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest httpRequest) {
        String token = extractBearerToken(httpRequest);
        authService.logout(token);
        return ResponseEntity.ok(new MessageResponse("Sesión cerrada exitosamente"));
    }

    /**
     * Retorna el perfil del usuario actualmente autenticado.
     * El correo se extrae del subject del JWT validado por Spring Security.
     *
     * @param authentication contexto de autenticación inyectado por Spring Security
     * @return DTO con datos del perfil del usuario sin información sensible
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication) {
        UserInfoResponse response = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Extrae el token JWT del header {@code Authorization: Bearer <token>}.
     *
     * @param request request HTTP del cliente
     * @return token JWT en formato compacto
     * @throws com.udmarketplace.auth.exception.InvalidTokenException si el header está ausente o malformado
     */
    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Token de autorización no encontrado o malformado");
        }
        return authHeader.substring(7);
    }
}
