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
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.model.Role;
import com.udmarketplace.auth.model.Vendedor;
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

    @Value("${app.auth.max-intentos-fallidos:5}")
    private int maxIntentosFallidos;

    @Value("${app.auth.minutos-bloqueo:30}")
    private int minutosBloqueo;

    @Override
    @Transactional
    public UserInfoResponse register(RegisterRequest request, MultipartFile pdfAutorizacion) {
        String correoInstitucional = normalizarCorreoInstitucional(request.getCorreoInstitu());
        validarDominioCorreo(correoInstitucional);

        if (userRepository.findByCorreoUsuario(correoInstitucional).isPresent()) {
            throw new OperacionNoPermitidaException("El correo institucional ya se encuentra registrado");
        }

        Role rolSolicitado;
        try {
            rolSolicitado = Role.valueOf(request.getPermisoUser().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new OperacionNoPermitidaException("El rol solicitado no es válido");
        }

        User nuevoUsuario = crearUsuarioPorRol(rolSolicitado);
        nuevoUsuario.setTipoDocumento(request.getTipoDocumento());
        nuevoUsuario.setNumeroDocumento(request.getNumeroDocumento());
        nuevoUsuario.setPrimerNombre(request.getPrimerNombre());
        nuevoUsuario.setSegundoNombre(request.getSegundoNombre());
        nuevoUsuario.setPrimerApellido(request.getPrimerApellido());
        nuevoUsuario.setSegundoApellido(request.getSegundoApellido());
        nuevoUsuario.setLugarNacimiento(request.getLugarNacimiento());
        nuevoUsuario.setFechaNacimiento(request.getFechaNacimiento());
        nuevoUsuario.setTelUser(request.getTelUser());
        nuevoUsuario.setGenero(request.getGenero());
        nuevoUsuario.setCorreoUsuario(correoInstitucional);
        nuevoUsuario.setPasswordUsua(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.setCodigoEstudiantil(request.getCodigoEstudiantil());
        nuevoUsuario.setEstadoAcademico(request.getEstadoAcademico());
        nuevoUsuario.setProyectoCurricular(request.getProyectoCurricular());
        nuevoUsuario.setRolUsua(rolSolicitado);
        nuevoUsuario.setEstadoCuenta(EstadoCuenta.ACTIVA);
        nuevoUsuario.setActivo(true);

        boolean menorEdad = request.getFechaNacimiento() != null
                && request.getFechaNacimiento().isAfter(LocalDate.now().minusYears(18));
        nuevoUsuario.setMenorEdad(menorEdad);

        if (menorEdad) {
            fileValidationService.validatePdf(pdfAutorizacion);
            try {
                nuevoUsuario.setPermisoUserMenor(pdfAutorizacion.getBytes());
            } catch (IOException ex) {
                throw new OperacionNoPermitidaException("No fue posible procesar el PDF de autorización");
            }
        }

        return userMapper.toUserInfoResponse(userRepository.save(nuevoUsuario));
    }

    @Override
    @Transactional
    public LoginStepResponse login(LoginRequest request, String ipOrigen) {
        String correoUsuario = normalizar(request.getCorreoUsuario());
        User user = userRepository.findByCorreoUsuario(correoUsuario).orElse(null);

        if (user != null && !user.isActivo()) {
            registrarIntento(correoUsuario, ipOrigen, false, user.getCodigoUsua());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        if (user != null && user.getBloqueadoHasta() != null
                && user.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            registrarIntento(correoUsuario, ipOrigen, false, user.getCodigoUsua());
            throw new AccountBlockedException("Cuenta bloqueada temporalmente. Intente después de: "
                    + user.getBloqueadoHasta());
        }

        if (user == null || !passwordEncoder.matches(request.getPasswordUsua(), user.getPasswordUsua())) {
            registrarIntento(correoUsuario, ipOrigen, false, user != null ? user.getCodigoUsua() : null);
            verificarYBloquearCuenta(user, correoUsuario);
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        twoFactorService.generateAndSendCode(user);
        log.debug("2FA enviado para: {}", user.getCorreoUsuario());

        return new LoginStepResponse("TWO_FACTOR_REQUIRED", user.getCorreoUsuario(),
                "Se ha enviado un código de verificación a tu email registrado");
    }

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

    private String normalizar(String correo) {
        return correo != null ? correo.trim().toLowerCase(Locale.ROOT) : null;
    }

    private void registrarIntento(String correo, String ip, boolean exitoso, Long codigoUsuario) {
        intentoFallidoRepo.save(IntentoFallidoAuth.builder()
                .correoIntentado(correo)
                .codigoUsuario(codigoUsuario)
                .ipOrigen(ip)
                .fechaHora(LocalDateTime.now())
                .exitoso(exitoso)
                .build());
    }

    private User crearUsuarioPorRol(Role rolSolicitado) {
        return switch (rolSolicitado) {
            case ADMINISTRADOR -> new Administrador();
            case VENDEDOR -> new Vendedor();
            case COMPRADOR -> new Comprador();
        };
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

    /**
     * Valida si la cuenta del usuario está habilitada para autenticarse.
     *
     * @param user usuario recuperado de base de datos
     * @return {@code true} si la cuenta está activa y no fue suspendida ni deshabilitada
     */
    private boolean estaCuentaHabilitada(User user) {
        EstadoCuenta estadoCuenta = user.getEstadoCuenta();
        boolean estadoActivo = estadoCuenta == null || estadoCuenta == EstadoCuenta.ACTIVA;
        return user.isActivo() && estadoActivo;
    }

    /**
     * Normaliza el correo institucional a minúsculas y sin espacios extremos.
     *
     * @param correo valor recibido en el request
     * @return correo normalizado
     */
    private String normalizarCorreoInstitucional(String correo) {
        return correo != null ? correo.trim().toLowerCase(Locale.ROOT) : null;
    }

    /**
     * Verifica que el correo pertenezca al dominio institucional permitido.
     *
     * @param correoInstitucional correo normalizado
     */
    private void validarDominioCorreo(String correoInstitucional) {
        if (correoInstitucional == null || !correoInstitucional.endsWith("@udistrital.edu.co")) {
            throw new OperacionNoPermitidaException(
                    "El correo institucional debe pertenecer al dominio @udistrital.edu.co");
        }
    }
}
