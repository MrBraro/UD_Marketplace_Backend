package com.udmarketplace.auth.service;

import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.service.impl.TwoFactorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoFactorServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TwoFactorServiceImpl twoFactorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(twoFactorService, "minutosExpiryTwoFactor", 10);
    }

    @Test
    void generateAndSendCode_debeGenerarCodigoDeSeisDigitosGuardarUsuarioYEnviarCorreo() {
        // Arrange: preparamos un usuario de prueba.
        User user = new User();
        user.setCorreoUsuario("cesar@udistrital.edu.co");

        LocalDateTime beforeGeneration = LocalDateTime.now();

        // Act: ejecutamos el método que genera y envía el código 2FA.
        twoFactorService.generateAndSendCode(user);

        // Assert: verificamos que el código exista.
        assertNotNull(user.getTwoFactorCode());

        // Assert: verificamos que el código tenga exactamente 6 dígitos.
        assertTrue(user.getTwoFactorCode().matches("\\d{6}"));

        // Assert: verificamos que la expiración haya sido asignada.
        assertNotNull(user.getTwoFactorExpiry());

        // Assert: la expiración debe ser posterior al momento de generación.
        assertTrue(user.getTwoFactorExpiry().isAfter(beforeGeneration));

        // Assert: con configuración de 10 minutos, no debería superar mucho ese rango.
        assertTrue(user.getTwoFactorExpiry().isBefore(beforeGeneration.plusMinutes(11)));

        // Assert: se debe guardar el usuario con el código generado.
        verify(userRepository).save(user);

        // Assert: se debe enviar el mismo código al correo del usuario.
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService).sendTwoFactorCode(
                eq("cesar@udistrital.edu.co"),
                codeCaptor.capture()
        );

        assertEquals(user.getTwoFactorCode(), codeCaptor.getValue());
    }

    @Test
    void validateCode_debeRetornarTrueCuandoCodigoEsCorrectoYNoHaExpirado() {
        // Arrange
        User user = new User();
        user.setCorreoUsuario("cesar@udistrital.edu.co");
        user.setTwoFactorCode("123456");
        user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));

        // Act
        boolean result = twoFactorService.validateCode(user, "123456");

        // Assert
        assertTrue(result);

        // En este servicio solo se valida.
        // La limpieza después del uso correcto ocurre en AuthServiceImpl.verifyTwoFactor().
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validateCode_debeRetornarFalseCuandoCodigoEsIncorrecto() {
        // Arrange
        User user = new User();
        user.setCorreoUsuario("cesar@udistrital.edu.co");
        user.setTwoFactorCode("123456");
        user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));

        // Act
        boolean result = twoFactorService.validateCode(user, "999999");

        // Assert
        assertFalse(result);

        // Un código incorrecto no debe limpiar el código real.
        assertEquals("123456", user.getTwoFactorCode());
        assertNotNull(user.getTwoFactorExpiry());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validateCode_debeRetornarFalseCuandoNoHayCodigoGuardado() {
        // Arrange
        User user = new User();
        user.setCorreoUsuario("cesar@udistrital.edu.co");
        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));

        // Act
        boolean result = twoFactorService.validateCode(user, "123456");

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validateCode_debeRetornarFalseCuandoCodigoIngresadoEsNull() {
        // Arrange
        User user = new User();
        user.setCorreoUsuario("cesar@udistrital.edu.co");
        user.setTwoFactorCode("123456");
        user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));

        // Act
        boolean result = twoFactorService.validateCode(user, null);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validateCode_debeInvalidarCodigoCuandoEstaExpirado() {
        // Arrange
        User user = new User();
        user.setCorreoUsuario("cesar@udistrital.edu.co");
        user.setTwoFactorCode("123456");
        user.setTwoFactorExpiry(LocalDateTime.now().minusMinutes(1));

        // Act
        boolean result = twoFactorService.validateCode(user, "123456");

        // Assert
        assertFalse(result);

        // RF14: si el código expiró, debe quedar invalidado.
        assertNull(user.getTwoFactorCode());
        assertNull(user.getTwoFactorExpiry());

        verify(userRepository).save(user);
    }
}