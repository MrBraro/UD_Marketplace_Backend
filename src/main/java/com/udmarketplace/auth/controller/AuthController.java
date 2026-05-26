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
 * <p>Endpoints expuestos alineados al diagrama ER y contratos definidos.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     *
     * <p>Paso 1 del login. Valida correoUsuario y passwordUsua (RF08).
     * Si son correctas, genera un código 2FA y lo envía al correoUsuario.
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
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication) {
        UserInfoResponse response = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Token de autorización no encontrado o malformado");
        }
        return authHeader.substring(7);
    }
}
