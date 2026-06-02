package com.udmarketplace.auth.service;

import com.udmarketplace.auth.dto.LoginResponse;
import com.udmarketplace.auth.dto.TwoFactorRequest;
import com.udmarketplace.auth.exception.InvalidCredentialsException;
import com.udmarketplace.auth.exception.TwoFactorException;
import com.udmarketplace.auth.mapper.UserMapper;
import com.udmarketplace.auth.model.Role;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.IntentoFallidoAuthRepository;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void verifyTwoFactor_debeInvalidarCodigoYGenerarTokenCuandoCodigoEsValido() {
        // Arrange: preparamos la solicitud 2FA.
        TwoFactorRequest request = new TwoFactorRequest();
        request.setCorreoUsuario("cesar@udistrital.edu.co");
        request.setTwoFactorCode("123456");

        // Arrange: preparamos un usuario con código 2FA activo.
        User user = new User();
        user.setCodigoUsua(10L);
        user.setCorreoUsuario("cesar@udistrital.edu.co");
        user.setRolUsua(Role.COMPRADOR);
        user.setTwoFactorCode("123456");
        user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));
        user.setBloqueadoHasta(LocalDateTime.now().plusMinutes(30));

        when(userRepository.findByCorreoUsuario("cesar@udistrital.edu.co"))
                .thenReturn(Optional.of(user));

        when(twoFactorService.validateCode(user, "123456"))
                .thenReturn(true);

        when(jwtUtil.generateToken("cesar@udistrital.edu.co", "COMPRADOR", 10L))
                .thenReturn("jwt-test-token");

        // Act: ejecutamos el segundo paso del login.
        LoginResponse response = authService.verifyTwoFactor(request);

        // Assert: debe retornar respuesta de login.
        assertNotNull(response);

        // Assert: RF14, el código usado correctamente debe quedar invalidado.
        assertNull(user.getTwoFactorCode());
        assertNull(user.getTwoFactorExpiry());

        // Assert: también limpia bloqueo previo después de autenticación exitosa.
        assertNull(user.getBloqueadoHasta());

        verify(userRepository).save(user);

        verify(jwtUtil).generateToken(
                "cesar@udistrital.edu.co",
                "COMPRADOR",
                10L
        );
    }

    @Test
    void verifyTwoFactor_debeLanzarTwoFactorExceptionCuandoCodigoEsInvalido() {
        // Arrange
        TwoFactorRequest request = new TwoFactorRequest();
        request.setCorreoUsuario("cesar@udistrital.edu.co");
        request.setTwoFactorCode("999999");

        User user = new User();
        user.setCodigoUsua(10L);
        user.setCorreoUsuario("cesar@udistrital.edu.co");
        user.setRolUsua(Role.COMPRADOR);
        user.setTwoFactorCode("123456");
        user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByCorreoUsuario("cesar@udistrital.edu.co"))
                .thenReturn(Optional.of(user));

        when(twoFactorService.validateCode(user, "999999"))
                .thenReturn(false);

        // Act + Assert
        assertThrows(
                TwoFactorException.class,
                () -> authService.verifyTwoFactor(request)
        );

        // Si el código es inválido, no debe limpiar, guardar ni generar token.
        assertEquals("123456", user.getTwoFactorCode());
        assertNotNull(user.getTwoFactorExpiry());

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void verifyTwoFactor_debeLanzarInvalidCredentialsExceptionCuandoUsuarioNoExiste() {
        // Arrange
        TwoFactorRequest request = new TwoFactorRequest();
        request.setCorreoUsuario("noexiste@udistrital.edu.co");
        request.setTwoFactorCode("123456");

        when(userRepository.findByCorreoUsuario("noexiste@udistrital.edu.co"))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.verifyTwoFactor(request)
        );

        verifyNoInteractions(twoFactorService);
        verifyNoInteractions(jwtUtil);
        verify(userRepository, never()).save(any(User.class));
    }
}