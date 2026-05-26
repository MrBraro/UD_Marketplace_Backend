package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.dto.LoginRequest;
import com.udmarketplace.auth.dto.LoginResponse;
import com.udmarketplace.auth.dto.LoginStepResponse;
import com.udmarketplace.auth.dto.TwoFactorRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.exception.InvalidCredentialsException;
import com.udmarketplace.auth.exception.TwoFactorException;
import com.udmarketplace.auth.mapper.UserMapper;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.auth.service.AuthService;
import com.udmarketplace.auth.service.TokenBlacklistService;
import com.udmarketplace.auth.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación principal del servicio de autenticación.
 *
 * <p>Orquesta el flujo completo de autenticación en dos pasos alineado con el diagrama ER:
 *
 * <pre>
 * Paso 1 — login() [RF08]:
 *   → Busca el usuario por correoUsuario
 *   → Valida la contraseña contra el hash passwordUsua
 *   → Genera y envía el código 2FA al correoUsuario
 *   → Retorna estado TWO_FACTOR_REQUIRED
 *
 * Paso 2 — verifyTwoFactor() [RF11]:
 *   → Busca el usuario por correoUsuario
 *   → Valida el código 2FA recibido
 *   → Limpia el código usado (previene reutilización)
 *   → Genera y retorna el JWT de sesión
 *
 * Logout — logout() [RF13, RF25]:
 *   → Delega al TokenBlacklistService para invalidar el token
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TwoFactorService twoFactorService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserMapper userMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginStepResponse login(LoginRequest request) {
        User user = userRepository.findByCorreoUsuario(request.getCorreoUsuario())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPasswordUsua(), user.getPasswordUsua())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        // Credenciales válidas (RF08) — generar y enviar código 2FA
        twoFactorService.generateAndSendCode(user);
        log.debug("Login exitoso para usuario '{}', código 2FA enviado", user.getCorreoUsuario());

        return new LoginStepResponse(
                "TWO_FACTOR_REQUIRED",
                user.getCorreoUsuario(),
                "Se ha enviado un código de verificación a tu email registrado"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginResponse verifyTwoFactor(TwoFactorRequest request) {
        User user = userRepository.findByCorreoUsuario(request.getCorreoUsuario())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!twoFactorService.validateCode(user, request.getTwoFactorCode())) {
            throw new TwoFactorException("Código de verificación inválido");
        }

        // Limpiar el código usado — previene reutilización
        user.setTwoFactorCode(null);
        userRepository.save(user);

        // Emitir JWT (RF11)
        String token = jwtUtil.generateToken(user.getCorreoUsuario(), user.getRolUsua().name());
        log.debug("JWT emitido para usuario '{}' con rol '{}'", user.getCorreoUsuario(), user.getRolUsua());

        return new LoginResponse(token, user.getCorreoUsuario(), user.getRolUsua().name(), "Bearer");
    }

    /** {@inheritDoc} */
    @Override
    public void logout(String token) {
        tokenBlacklistService.invalidateToken(token);
        log.debug("Token invalidado (logout)");
    }

    /** {@inheritDoc} */
    @Override
    public UserInfoResponse getCurrentUser(String correoUsuario) {
        User user = userRepository.findByCorreoUsuario(correoUsuario)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));
        return userMapper.toUserInfoResponse(user);
    }
}
