package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.dto.LoginRequest;
import com.udmarketplace.auth.dto.LoginResponse;
import com.udmarketplace.auth.dto.LoginStepResponse;
import com.udmarketplace.auth.dto.RegisterRequest;
import com.udmarketplace.auth.dto.TwoFactorRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.exception.AccountBlockedException;
import com.udmarketplace.auth.exception.InvalidCredentialsException;
import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.exception.TwoFactorException;
import com.udmarketplace.auth.mapper.UserMapper;
import com.udmarketplace.auth.model.Administrador;
import com.udmarketplace.auth.model.Comprador;
import com.udmarketplace.auth.model.EstadoCuenta;
import com.udmarketplace.auth.model.IntentoFallidoAuth;
import com.udmarketplace.auth.model.Role;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.model.Vendedor;
import com.udmarketplace.auth.repository.IntentoFallidoAuthRepository;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.auth.service.AuthService;
import com.udmarketplace.auth.service.FileValidationService;
import com.udmarketplace.auth.service.PythonCouponClientService;
import com.udmarketplace.auth.service.PythonEmailClientService;
import com.udmarketplace.auth.service.TokenBlacklistService;
import com.udmarketplace.auth.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Locale;

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
    private final PythonEmailClientService pythonEmailClient;
    private final PythonCouponClientService pythonCouponClient;

    @Value("${app.auth.max-intentos-fallidos:5}")
    private int maxIntentosFallidos;

    @Value("${app.auth.minutos-bloqueo:30}")
    private int minutosBloqueo;

    // ─────────────────────────────────────────────────────────────────────
    // register
    // ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserInfoResponse register(RegisterRequest request, MultipartFile pdfAutorizacion) {
        String correoInstitucional = normalizar(request.getCorreoInstitu());
        validarDominioCorreo(correoInstitucional);

        if (userRepository.findByCorreoUsuario(correoInstitucional).isPresent()) {
            throw new OperacionNoPermitidaException("El correo institucional ya se encuentra registrado");
        }
        if (request.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        if (request.getPermisoUser() == null || request.getPermisoUser().isBlank()) {
            throw new IllegalArgumentException("El rol del usuario es obligatorio");
        }

        final boolean menorEdad = esMenorDeEdad(request.getFechaNacimiento());
        final Role rol;
        try {
            rol = Role.valueOf(request.getPermisoUser().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("El rol solicitado no es válido");
        }

        if (menorEdad) {
            fileValidationService.validatePdf(pdfAutorizacion);
        }

        // CRÍTICO: instanciar el subtipo correcto para que JPA JOINED genere la fila del subtipo
        User user = crearSubtipoUsuario(rol);
        user.setPrimerNombre(request.getPrimerNombre());
        user.setSegundoNombre(request.getSegundoNombre());
        user.setPrimerApellido(request.getPrimerApellido());
        user.setSegundoApellido(request.getSegundoApellido());
        user.setTipoDocumento(request.getTipoDocumento());
        user.setNumeroDocumento(request.getNumeroDocumento());
        user.setLugarNacimiento(request.getLugarNacimiento());
        user.setFechaNacimiento(request.getFechaNacimiento());
        user.setCorreoUsuario(correoInstitucional);
        user.setPasswordUsua(passwordEncoder.encode(request.getPassword()));
        user.setGenero(request.getGenero());
        user.setTelUser(request.getTelUser());
        user.setActivo(true);
        user.setEstadoCuenta(EstadoCuenta.ACTIVA);
        user.setMenorEdad(menorEdad);
        user.setRolUsua(rol);
        user.setCodigoEstudiantil(request.getCodigoEstudiantil());
        user.setEstadoAcademico(request.getEstadoAcademico());
        user.setProyectoCurricular(request.getProyectoCurricular());

        if (menorEdad) {
            try {
                user.setPermisoUserMenor(pdfAutorizacion.getBytes());
            } catch (IOException e) {
                throw new IllegalArgumentException("No fue posible procesar el PDF de autorización");
            }
        }

        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new OperacionNoPermitidaException("El correo institucional ya se encuentra registrado");
        }

        log.info("Usuario registrado: {}", savedUser.getCorreoUsuario());

        // Notificar al mail service (degradación elegante si no está disponible)
        pythonEmailClient.enviarConfirmacionRegistro(savedUser);

        // Crear cupón de cumpleaños en el coupon service
        pythonCouponClient.crearCuponCumpleanios(savedUser.getCorreoUsuario());

        return new UserInfoResponse(
                savedUser.getCodigoUsua(),
                savedUser.getCorreoUsuario(),
                savedUser.getRolUsua() != null ? savedUser.getRolUsua().name() : null,
                savedUser.getPrimerNombre(),
                savedUser.getSegundoNombre(),
                savedUser.getPrimerApellido(),
                savedUser.getSegundoApellido(),
                savedUser.getGenero(),
                savedUser.getFechaNacimiento()
        );
    }

    // ─────────────────────────────────────────────────────────────────────
    // login
    // ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginStepResponse login(LoginRequest request, String ipOrigen) {
        String correoUsuario = normalizar(request.getCorreoUsuario());
        User user = userRepository.findByCorreoUsuario(correoUsuario).orElse(null);

        if (user != null && !estaCuentaHabilitada(user)) {
            registrarIntento(correoUsuario, user.getCodigoUsua(), ipOrigen, false);
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        if (user != null && user.getBloqueadoHasta() != null
                && user.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            registrarIntento(correoUsuario, user.getCodigoUsua(), ipOrigen, false);
            throw new AccountBlockedException("Cuenta bloqueada temporalmente. Intente después de: "
                    + user.getBloqueadoHasta());
        }

        if (user == null || !passwordEncoder.matches(request.getPasswordUsua(), user.getPasswordUsua())) {
            registrarIntento(correoUsuario, user != null ? user.getCodigoUsua() : null, ipOrigen, false);
            verificarYBloquearCuenta(user, correoUsuario);
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        twoFactorService.generateAndSendCode(user);
        log.debug("2FA enviado para: {}", user.getCorreoUsuario());

        return new LoginStepResponse("TWO_FACTOR_REQUIRED", user.getCorreoUsuario(),
                "Se ha enviado un código de verificación a tu email registrado");
    }

    // ─────────────────────────────────────────────────────────────────────
    // verifyTwoFactor
    // ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse verifyTwoFactor(TwoFactorRequest request) {
        String correoUsuario = normalizar(request.getCorreoUsuario());
        User user = userRepository.findByCorreoUsuario(correoUsuario)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!twoFactorService.validateCode(user, request.getTwoFactorCode())) {
            throw new TwoFactorException("Código de verificación inválido o expirado");
        }

        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(null);
        user.setBloqueadoHasta(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getCorreoUsuario(), user.getRolUsua().name(), user.getCodigoUsua());
        log.debug("JWT emitido para: {}", user.getCorreoUsuario());

        return new LoginResponse(token, user.getCorreoUsuario(), user.getRolUsua().name(), "Bearer");
    }

    @Override
    public void logout(String token) {
        tokenBlacklistService.invalidateToken(token);
    }

    @Override
    public UserInfoResponse getCurrentUser(String correoUsuario) {
        User user = userRepository.findByCorreoUsuario(correoUsuario)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));
        return userMapper.toUserInfoResponse(user);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Métodos privados
    // ─────────────────────────────────────────────────────────────────────

    private User crearSubtipoUsuario(Role rol) {
        return switch (rol) {
            case VENDEDOR -> {
                Vendedor v = new Vendedor();
                v.setCalificacion(BigDecimal.ZERO);
                yield v;
            }
            case COMPRADOR -> new Comprador();
            case ADMINISTRADOR -> new Administrador();
        };
    }

    private boolean esMenorDeEdad(LocalDate fechaNacimiento) {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() < 18;
    }

    private boolean estaCuentaHabilitada(User user) {
        EstadoCuenta estado = user.getEstadoCuenta();
        return user.isActivo() && (estado == null || estado == EstadoCuenta.ACTIVA);
    }

    private String normalizar(String correo) {
        return correo != null ? correo.trim().toLowerCase(Locale.ROOT) : null;
    }

    private void validarDominioCorreo(String correo) {
        if (correo == null || !correo.endsWith("@udistrital.edu.co")) {
            throw new OperacionNoPermitidaException(
                    "El correo debe pertenecer al dominio @udistrital.edu.co");
        }
    }

    private void registrarIntento(String correo, Long codigoUsuario, String ip, boolean exitoso) {
        intentoFallidoRepo.save(IntentoFallidoAuth.builder()
                .correoIntentado(correo)
                .codigoUsuario(codigoUsuario)
                .ipOrigen(ip)
                .fechaHora(LocalDateTime.now())
                .exitoso(exitoso)
                .build());
    }

    private void verificarYBloquearCuenta(User user, String correo) {
        if (user == null) return;
        LocalDateTime ventana = LocalDateTime.now().minusMinutes(10);
        long fallos = intentoFallidoRepo.contarIntentosFallidosDesde(correo, ventana);
        if (fallos >= maxIntentosFallidos) {
            user.setBloqueadoHasta(LocalDateTime.now().plusMinutes(minutosBloqueo));
            userRepository.save(user);
            log.warn("Cuenta '{}' bloqueada hasta {}", correo, user.getBloqueadoHasta());
        }
    }
}
