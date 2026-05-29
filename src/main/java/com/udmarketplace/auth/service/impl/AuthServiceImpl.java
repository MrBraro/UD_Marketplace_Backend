package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.dto.LoginRequest;
import com.udmarketplace.auth.dto.LoginResponse;
import com.udmarketplace.auth.dto.LoginStepResponse;
import com.udmarketplace.auth.dto.RegisterRequest;
import com.udmarketplace.auth.dto.TwoFactorRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.dto.UserResponse;
import com.udmarketplace.auth.exception.AccountBlockedException;
import com.udmarketplace.auth.exception.InvalidCredentialsException;
import com.udmarketplace.auth.exception.TwoFactorException;
import com.udmarketplace.auth.mapper.UserMapper;
import com.udmarketplace.auth.model.IntentoFallidoAuth;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.model.Role;
import com.udmarketplace.auth.repository.IntentoFallidoAuthRepository;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.auth.service.AuthService;
import com.udmarketplace.auth.service.FileValidationService;
import com.udmarketplace.auth.service.TokenBlacklistService;
import com.udmarketplace.auth.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Implementación principal del servicio de autenticación del sistema UD Marketplace.
 *
 * <p>Orquesta el flujo completo de autenticación en dos factores y el registro de usuarios:
 * <pre>
 * 
 * Registro — register():
 *   → Valida que el correo no esté previamente registrado
 *   → Calcula si el usuario es menor de edad a partir de la fecha de nacimiento
 *   → Exige y valida PDF de autorización si aplica
 *   → Codifica la contraseña con bcrypt antes de persistir
 *   → Guarda el usuario activo con su información básica
 *
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
 * @version 1.2
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
    private final FileValidationService fileValidationService;

    /** Número máximo de intentos fallidos antes de bloquear la cuenta. */
    @Value("${app.auth.max-intentos-fallidos:5}")
    private int maxIntentosFallidos;

    /** Duración en minutos del bloqueo temporal de cuenta. */
    @Value("${app.auth.minutos-bloqueo:30}")
    private int minutosBloqueo;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Calcula si el usuario es menor de edad a partir de su fecha de nacimiento.
     * Si el usuario es menor, exige un PDF de autorización válido del representante legal.
     * La contraseña se transforma con bcrypt antes de guardarse.
     *
     * @param request datos de registro enviados por el cliente
     * @param pdfAutorizacion archivo PDF de autorización para usuarios menores de edad
     * @return respuesta con la información básica del usuario creado
     */
    @Override
    @Transactional
@Override
@Transactional
public UserResponse register(RegisterRequest request, MultipartFile pdfAutorizacion) {
    if (userRepository.findByCorreoUsuario(request.getCorreoInstitu()).isPresent()) {
        throw new IllegalArgumentException("El correo institucional ya se encuentra registrado");
    }

    if (request.getFechaNacimiento() == null) {
        throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
    }

    if (request.getPermisoUser() == null || request.getPermisoUser().trim().isEmpty()) {
        throw new IllegalArgumentException("El rol del usuario es obligatorio");
    }

    final boolean menorEdad = esMenorDeEdad(request.getFechaNacimiento());
    final Role rol;

    try {
        rol = Role.valueOf(request.getPermisoUser().trim().toUpperCase(java.util.Locale.ROOT));
    } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException("El rol solicitado no es válido");
    }

    if (menorEdad) {
        fileValidationService.validatePdf(pdfAutorizacion);
    }

    User user = new User();
    user.setPrimerNomb(request.getPrimerNombre());
    user.setSegundoNom(request.getSegundoNombre());
    user.setPrimerApel(request.getPrimerApellido());
    user.setSegundoApel(request.getSegundoApellido());
    user.setFechaNacimiento(request.getFechaNacimiento());
    user.setCorreoUsuario(request.getCorreoInstitu());
    user.setPasswordUsua(passwordEncoder.encode(request.getPassword()));
    user.setGenero(request.getGenero());
    user.setTelUser(request.getTelUser());
    user.setActivo(true);
    user.setMenorEdad(menorEdad);
    user.setRolUsua(rol);

    if (menorEdad) {
        try {
            user.setPermisoUserMenor(pdfAutorizacion.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("No fue posible procesar el PDF de autorización");
        }
    } else {
        user.setPermisoUserMenor(null);
    }

    User savedUser = userRepository.save(user);
    log.info("Usuario registrado exitosamente con correo '{}'", savedUser.getCorreoUsuario());

    return UserResponse.builder()
            .codigoUser(savedUser.getCodigoUsua())
            .correoInstitu(savedUser.getCorreoUsuario())
            .permisoUser(savedUser.getRolUsua().name())
            .menorEdad(savedUser.getMenorEdad())
            .activo(savedUser.getActivo())
            .build();
}

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

    /**
     * Determina si una persona es menor de edad con base en su fecha de nacimiento.
     *
     * @param fechaNacimiento fecha de nacimiento del usuario
     * @return {@code true} si la edad calculada es menor a 18 años
     */
    private boolean esMenorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18;
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
