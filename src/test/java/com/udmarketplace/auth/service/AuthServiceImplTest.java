package com.udmarketplace.auth.service;

import com.udmarketplace.auth.dto.LoginRequest;
import com.udmarketplace.auth.dto.RegisterRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.exception.InvalidCredentialsException;
import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.mapper.UserMapper;
import com.udmarketplace.auth.model.EstadoCuenta;
import com.udmarketplace.auth.model.IntentoFallidoAuth;
import com.udmarketplace.auth.model.Role;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.IntentoFallidoAuthRepository;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImplTest")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private IntentoFallidoAuthRepository intentoFallidoRepo;

    @Mock
    private FileValidationService fileValidationService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "maxIntentosFallidos", 5);
        ReflectionTestUtils.setField(authService, "minutosBloqueo", 30);
    }

    @Test
    void register_persisteCamposNuevoUsuarioYNormalizaCorreo() throws IOException {
        RegisterRequest request = RegisterRequest.builder()
                .tipoDocumento("CC")
                .numeroDocumento("10101010")
                .primerNombre("Laura")
                .segundoNombre("María")
                .primerApellido("Gómez")
                .segundoApellido("Pérez")
                .lugarNacimiento("Bogotá")
                .fechaNacimiento(LocalDate.of(2000, 6, 15))
                .telUser("3001234567")
                .genero("Femenino")
                .correoInstitu("Estudiante@UDISTRITAL.edu.co")
                .password("Secreta123!")
                .codigoEstudiantil("20241001001")
                .estadoAcademico("Activo")
                .proyectoCurricular("Ingeniería de Sistemas")
                .permisoUser(Role.COMPRADOR.name())
                .build();

        when(userRepository.findByCorreoUsuario("estudiante@udistrital.edu.co"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secreta123!")).thenReturn("hash-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setCodigoUsua(15L);
            return saved;
        });

        UserInfoResponse response = authService.register(request, mockPdf());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getCorreoUsuario()).isEqualTo("estudiante@udistrital.edu.co");
        assertThat(saved.getTipoDocumento()).isEqualTo("CC");
        assertThat(saved.getNumeroDocumento()).isEqualTo("10101010");
        assertThat(saved.getLugarNacimiento()).isEqualTo("Bogotá");
        assertThat(saved.getCodigoEstudiantil()).isEqualTo("20241001001");
        assertThat(saved.getEstadoAcademico()).isEqualTo("Activo");
        assertThat(saved.getProyectoCurricular()).isEqualTo("Ingeniería de Sistemas");
        assertThat(saved.getEstadoCuenta()).isEqualTo(EstadoCuenta.ACTIVA);
        assertThat(response.getCorreoUsuario()).isEqualTo("estudiante@udistrital.edu.co");
        assertThat(response.getRolUsua()).isEqualTo(Role.COMPRADOR.name());
        verify(fileValidationService, never()).validatePdf(any());
    }

    @Test
    void register_correoDuplicado_lanzaOperacionNoPermitidaException() {
        RegisterRequest request = RegisterRequest.builder()
                .tipoDocumento("CC")
                .numeroDocumento("10101010")
                .primerNombre("Laura")
                .primerApellido("Gómez")
                .lugarNacimiento("Bogotá")
                .fechaNacimiento(LocalDate.of(2000, 6, 15))
                .telUser("3001234567")
                .genero("Femenino")
                .correoInstitu("duplicado@udistrital.edu.co")
                .password("Secreta123!")
                .codigoEstudiantil("20241001001")
                .estadoAcademico("Activo")
                .proyectoCurricular("Ingeniería de Sistemas")
                .permisoUser(Role.COMPRADOR.name())
                .build();

        when(userRepository.findByCorreoUsuario("duplicado@udistrital.edu.co"))
                .thenReturn(Optional.of(new User()));

        OperacionNoPermitidaException ex = assertThrows(
                OperacionNoPermitidaException.class,
                () -> authService.register(request, mockPdf()));

        assertThat(ex.getMessage()).isEqualTo("El correo institucional ya se encuentra registrado");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_correoFueraDelDominio_lanzaOperacionNoPermitidaException() {
        RegisterRequest request = RegisterRequest.builder()
                .tipoDocumento("CC")
                .numeroDocumento("10101010")
                .primerNombre("Laura")
                .primerApellido("Gómez")
                .lugarNacimiento("Bogotá")
                .fechaNacimiento(LocalDate.of(2000, 6, 15))
                .telUser("3001234567")
                .genero("Femenino")
                .correoInstitu("usuario@gmail.com")
                .password("Secreta123!")
                .codigoEstudiantil("20241001001")
                .estadoAcademico("Activo")
                .proyectoCurricular("Ingeniería de Sistemas")
                .permisoUser(Role.COMPRADOR.name())
                .build();

        OperacionNoPermitidaException ex = assertThrows(
                OperacionNoPermitidaException.class,
                () -> authService.register(request, mockPdf()));

        assertThat(ex.getMessage()).isEqualTo("El correo institucional debe pertenecer al dominio @udistrital.edu.co");
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_cuentaSuspendida_rechazaAutenticacion() {
        User user = new User();
        user.setCodigoUsua(33L);
        user.setCorreoUsuario("suspendido@udistrital.edu.co");
        user.setPasswordUsua("hash");
        user.setActivo(false);
        user.setEstadoCuenta(EstadoCuenta.SUSPENDIDA);

        when(userRepository.findByCorreoUsuario("suspendido@udistrital.edu.co"))
                .thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.setCorreoUsuario("SUSPENDIDO@UDISTRITAL.EDU.CO");
        request.setPasswordUsua("Secreta123!");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request, "127.0.0.1"));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(twoFactorService, never()).generateAndSendCode(any());
        verify(intentoFallidoRepo).save(any(IntentoFallidoAuth.class));
    }

    private MultipartFile mockPdf() {
        return org.mockito.Mockito.mock(MultipartFile.class);
    }
}