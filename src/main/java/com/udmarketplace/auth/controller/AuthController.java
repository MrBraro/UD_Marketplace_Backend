package com.udmarketplace.auth.controller;

import com.udmarketplace.auth.dto.LoginRequest;
import com.udmarketplace.auth.dto.LoginResponse;
import com.udmarketplace.auth.dto.LoginStepResponse;
import com.udmarketplace.auth.dto.MessageResponse;
import com.udmarketplace.auth.dto.TwoFactorRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.exception.InvalidTokenException;
import com.udmarketplace.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación y gestión de sesión.
 *
 * <p>Endpoints expuestos:
 * <ul>
 *   <li>POST /api/auth/login          — Validar credenciales (RF08)</li>
 *   <li>POST /api/auth/verifyTwoFactor — Validar 2FA y emitir JWT (RF11)</li>
 *   <li>POST /api/auth/logout          — Cerrar sesión e invalidar token (RF13, RF25)</li>
 *   <li>GET  /api/auth/me              — Información del usuario autenticado (RF24)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     *
     * <p>Paso 1 del login. Valida username y password (RF08).
     * Si son correctas, genera un código 2FA y lo envía al email del usuario.
     *
     * <p>No requiere autenticación previa.
     *
     * @param request body con username y password
     * @return 200 con step=TWO_FACTOR_REQUIRED | 401 si credenciales inválidas
     */
    @PostMapping("/login")
    public ResponseEntity<LoginStepResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginStepResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/verifyTwoFactor
     *
     * <p>Paso 2 del login. Valida el código 2FA recibido por email (RF11).
     * Si es correcto, emite el JWT de sesión.
     *
     * <p>No requiere autenticación previa.
     *
     * @param request body con username y twoFactorCode
     * @return 200 con JWT | 401 si el código es inválido
     */
    @PostMapping("/verifyTwoFactor")
    public ResponseEntity<LoginResponse> verifyTwoFactor(@Valid @RequestBody TwoFactorRequest request) {
        LoginResponse response = authService.verifyTwoFactor(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout
     *
     * <p>Cierra la sesión del usuario autenticado (RF13, RF25).
     * Extrae el JWT del header Authorization y lo agrega a la blacklist.
     * El token queda inválido inmediatamente para futuros requests.
     *
     * <p>Requiere header: {@code Authorization: Bearer <token>}
     *
     * @param httpRequest request HTTP del que se extrae el token
     * @return 200 con mensaje de confirmación | 401 si no hay token válido
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest httpRequest) {
        String token = extractBearerToken(httpRequest);
        authService.logout(token);
        return ResponseEntity.ok(new MessageResponse("Sesión cerrada exitosamente"));
    }

    /**
     * GET /api/auth/me
     *
     * <p>Retorna la información del usuario autenticado actualmente (RF24).
     * Accesible por cualquier usuario con JWT válido (ADMIN, SELLER, BUYER).
     *
     * <p>Requiere header: {@code Authorization: Bearer <token>}
     *
     * @param authentication contexto de autenticación inyectado por Spring Security
     * @return 200 con datos del usuario | 401 si el token es inválido o expiró
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication) {
        UserInfoResponse response = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     * Lanza {@link InvalidTokenException} si el header está ausente o malformado.
     */
    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Token de autorización no encontrado o malformado");
        }
        return authHeader.substring(7);
    }
}
