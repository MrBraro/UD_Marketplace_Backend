package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.dto.LoginRequest;
import com.udmarketplace.auth.dto.LoginResponse;
import com.udmarketplace.auth.dto.LoginStepResponse;
import com.udmarketplace.auth.dto.TwoFactorRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.exception.AccountBlockedException;
import com.udmarketplace.auth.exception.InvalidCredentialsException;
import com.udmarketplace.auth.exception.TwoFactorException;
import com.udmarketplace.auth.mapper.UserMapper;
import com.udmarketplace.auth.model.IntentoFallidoAuth;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.IntentoFallidoAuthRepository;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.auth.service.AuthService;
import com.udmarketplace.auth.service.TokenBlacklistService;
import com.udmarketplace.auth.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación principal del servicio de autenticación del sistema UD Marketplace.
 *
 * <p>Orquesta el flujo completo de autenticación en dos factores:
 * <pre>
 * Paso 1 — login():
 *   → Verifica bloqueo temporal de la cuenta
 *   → Valida correo y contraseña con bcrypt
 *   → Registra el intento
 *   → Bloquea si se supera el máximo de intentos 
 *   → Genera y envía código 2FA con expiración de 10 min
 *
 * Paso 2 — verifyTwoFactor():
 *   → Valida el código 2FA y su expiración
 *   → Limpia el código para prevenir reutilización
 *   → Emite JWT con userId, correo y rol 
 *
 * logout():
 *   → Delega al TokenBlacklistService para invalidar el token
 * </pre>
 *
 * @version 1.1
 * @since 2026-05-28
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
    private final IntentoFallidoAuthRepository intentoFallidoRepo;

    /** Número máximo de intentos fallidos antes de bloquear la cuenta. */
    @Value("${app.auth.max-intentos-fallidos:5}")
    private int maxIntentosFallidos;

    /** Duración en minutos del bloqueo temporal de cuenta. */
    @Value("${app.auth.minutos-bloqueo:30}")
    private int minutosBloqueo;

    /**
     * {@inheritDoc}
     *
     * <p>Implementa registro de intentos y bloqueo temporal.
     */
    @Override
    @Transactional
    public LoginStepResponse login(LoginRequest request, String ipOrigen) {
        User user = userRepository.findByCorreoUsuario(request.getCorreoUsuario()).orElse(null);

        // verificar bloqueo antes de intentar validar credenciales
        if (user != null && user.getBloqueadoHasta() != null
                && user.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            registrarIntento(request.getCorreoUsuario(), ipOrigen, false);
            throw new AccountBlockedException(
                    "Cuenta bloqueada temporalmente. Intente de nuevo después de: "
                    + user.getBloqueadoHasta());
        }

        if (user == null || !passwordEncoder.matches(request.getPasswordUsua(), user.getPasswordUsua())) {
            // registrar el intento fallido con IP, fecha y hora
            registrarIntento(request.getCorreoUsuario(), ipOrigen, false);
            // bloquear si se alcanzó el umbral de intentos
            verificarYBloquearCuenta(user, request.getCorreoUsuario());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        twoFactorService.generateAndSendCode(user);
        log.debug("Login exitoso para '{}', código 2FA enviado", user.getCorreoUsuario());

        return new LoginStepResponse(
                "TWO_FACTOR_REQUIRED",
                user.getCorreoUsuario(),
                "Se ha enviado un código de verificación a tu email registrado"
        );
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementa incluyendo {@code userId} en el JWT generado.
     */
    @Override
    @Transactional
    public LoginResponse verifyTwoFactor(TwoFactorRequest request) {
        User user = userRepository.findByCorreoUsuario(request.getCorreoUsuario())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!twoFactorService.validateCode(user, request.getTwoFactorCode())) {
            throw new TwoFactorException("Código de verificación inválido o expirado");
        }

        // Limpiar código y bloqueo previo tras autenticación exitosa
        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(null);
        user.setBloqueadoHasta(null);
        userRepository.save(user);

        // incluir userId en el JWT para asociar el token con el usuario
        String token = jwtUtil.generateToken(
                user.getCorreoUsuario(),
                user.getRolUsua().name(),
                user.getCodigoUsua()
        );
        log.debug("JWT emitido para '{}' con rol '{}'", user.getCorreoUsuario(), user.getRolUsua());

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


    // Métodos privados de auditoría y control de acceso
    
    /**
     * Persiste un registro de intento de autenticación para auditoría (REQ-02).
     *
     * @param correo   correo electrónico utilizado en el intento
     * @param ip       dirección IP de origen de la solicitud
     * @param exitoso  {@code true} si el intento fue exitoso
     */
    private void registrarIntento(String correo, String ip, boolean exitoso) {
        intentoFallidoRepo.save(IntentoFallidoAuth.builder()
                .correoIntentado(correo)
                .ipOrigen(ip)
                .fechaHora(LocalDateTime.now())
                .exitoso(exitoso)
                .build());
    }

    /**
     * Verifica si el usuario alcanzó el máximo de intentos fallidos en la
     * ventana de 10 minutos y, de ser así, bloquea la cuenta (REQ-03).
     *
     * @param user   entidad del usuario (puede ser {@code null} si el correo no existe)
     * @param correo correo electrónico que se intentó usar
     */
    private void verificarYBloquearCuenta(User user, String correo) {
        if (user == null) return;
        LocalDateTime ventana = LocalDateTime.now().minusMinutes(10);
        long fallos = intentoFallidoRepo.contarIntentosFallidosDesde(correo, ventana);
        if (fallos >= maxIntentosFallidos) {
            user.setBloqueadoHasta(LocalDateTime.now().plusMinutes(minutosBloqueo));
            userRepository.save(user);
            log.warn("Cuenta '{}' bloqueada temporalmente hasta {}", correo, user.getBloqueadoHasta());
        }
    }
}
