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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
 *   <li>{@code GET  /api/auth/perfil}               — (Opcional) alias o endpoint de perfil</li>
 * </ul>
 *
 * @version 1.0
 * @since 2026-05-28
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación y Sesión", description = "Endpoints para inicio de sesión (2FA), cierre de sesión, perfil de usuario y recuperación de contraseña")
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
    @Operation(
            summary = "Paso 1: Validar credenciales y enviar 2FA",
            description = "Valida el correo y contraseña del usuario. Si son correctos, genera y envía un código 2FA de 6 dígitos al correo registrado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Credenciales válidas, se requiere validación 2FA (Paso 2)"),
                    @ApiResponse(responseCode = "400", description = "Formato de solicitud inválido"),
                    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta bloqueada")
            }
    )
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
    @Operation(
            summary = "Solicitar recuperación de contraseña",
            description = "Registra una solicitud de recuperación de contraseña y envía un correo con un token único de restablecimiento. Responde con HTTP 200 por motivos de seguridad, exista o no el correo.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Solicitud procesada")
            }
    )
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
    @Operation(
            summary = "Restablecer contraseña con token",
            description = "Establece una nueva contraseña para la cuenta usando un token de recuperación válido y no expirado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Token inválido, expirado o ya utilizado, o contraseña no cumple con requerimientos mínimos")
            }
    )
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
    @Operation(
            summary = "Paso 2: Verificar código 2FA y emitir JWT",
            description = "Valida el código de 6 dígitos enviado por correo. Si es válido, emite un token de sesión JWT con los detalles del usuario y sus roles.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Autenticación exitosa y JWT emitido"),
                    @ApiResponse(responseCode = "401", description = "Código de verificación inválido o expirado")
            }
    )
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
    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida el token JWT activo colocándolo en la lista negra (blacklist), impidiendo su uso posterior.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sesión cerrada y token invalidado exitosamente"),
                    @ApiResponse(responseCode = "401", description = "Token ausente o malformado")
            }
    )
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
    @Operation(
            summary = "Obtener perfil de usuario autenticado",
            description = "Retorna la información del perfil del usuario autenticado que corresponde al token de sesión válido enviado en las cabeceras.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Datos de perfil de usuario retornados"),
                    @ApiResponse(responseCode = "401", description = "No autenticado o token inválido")
            }
    )
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
