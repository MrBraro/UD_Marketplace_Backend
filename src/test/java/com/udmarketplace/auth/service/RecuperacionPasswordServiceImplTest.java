package com.udmarketplace.auth.service;

import com.udmarketplace.auth.exception.InvalidTokenException;
import com.udmarketplace.auth.model.TokenRecuperacion;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.TokenRecuperacionRepository;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.service.impl.RecuperacionPasswordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para recuperación de contraseña.
 *
 * <p>Cubre RNF05 para tokens de recuperación:
 * expiración configurable y no reutilización después de ser usados.
 */
@ExtendWith(MockitoExtension.class)
class RecuperacionPasswordServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRecuperacionRepository tokenRecuperacionRepo;

    @Mock
    private PythonEmailClientService pythonEmailClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RecuperacionPasswordServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "minutosExpiry", 30);
    }

    @Test
    void solicitarRecuperacion_debeGenerarTokenConExpiracionConfigurableYEnviarCorreo() {
        User user = usuario();

        when(userRepository.findByCorreoUsuario("cesar@udistrital.edu.co"))
                .thenReturn(Optional.of(user));

        service.solicitarRecuperacion("cesar@udistrital.edu.co");

        ArgumentCaptor<TokenRecuperacion> captor = ArgumentCaptor.forClass(TokenRecuperacion.class);
        verify(tokenRecuperacionRepo).save(captor.capture());

        TokenRecuperacion tokenGuardado = captor.getValue();

        assertThat(tokenGuardado.getUsuario()).isEqualTo(user);
        assertThat(tokenGuardado.getToken()).isNotBlank();
        assertThat(tokenGuardado.isUsado()).isFalse();
        assertThat(tokenGuardado.getFechaExpiracion())
                .isAfter(LocalDateTime.now().plusMinutes(29))
                .isBefore(LocalDateTime.now().plusMinutes(31));

        verify(pythonEmailClient).enviarCorreoRecuperacion(
                "cesar@udistrital.edu.co",
                tokenGuardado.getToken(),
                "César"
        );
    }

    @Test
    void solicitarRecuperacion_usuarioNoExiste_noDebeGenerarTokenNiEnviarCorreo() {
        when(userRepository.findByCorreoUsuario("noexiste@udistrital.edu.co"))
                .thenReturn(Optional.empty());

        service.solicitarRecuperacion("noexiste@udistrital.edu.co");

        verify(tokenRecuperacionRepo, never()).save(org.mockito.ArgumentMatchers.any());
        verify(pythonEmailClient, never()).enviarCorreoRecuperacion(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void resetearPassword_tokenValido_debeActualizarPasswordYMarcarTokenComoUsado() {
        User user = usuario();
        TokenRecuperacion token = TokenRecuperacion.builder()
                .usuario(user)
                .token("token-valido")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .build();

        when(tokenRecuperacionRepo.findByTokenAndUsadoFalse("token-valido"))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NuevaPassword123"))
                .thenReturn("password-hasheada");

        service.resetearPassword("token-valido", "NuevaPassword123");

        assertThat(user.getPasswordUsua()).isEqualTo("password-hasheada");
        assertThat(token.isUsado()).isTrue();

        verify(userRepository).save(user);
        verify(tokenRecuperacionRepo).save(token);
    }

    @Test
    void resetearPassword_tokenExpirado_debeLanzarExcepcionYNoActualizarPassword() {
        User user = usuario();
        TokenRecuperacion token = TokenRecuperacion.builder()
                .usuario(user)
                .token("token-expirado")
                .fechaExpiracion(LocalDateTime.now().minusMinutes(1))
                .usado(false)
                .build();

        when(tokenRecuperacionRepo.findByTokenAndUsadoFalse("token-expirado"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetearPassword("token-expirado", "NuevaPassword123"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("ha expirado");

        assertThat(token.isUsado()).isFalse();

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(tokenRecuperacionRepo, never()).save(org.mockito.ArgumentMatchers.any());
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void resetearPassword_tokenUsadoONoExistente_debeLanzarExcepcion() {
        when(tokenRecuperacionRepo.findByTokenAndUsadoFalse("token-usado"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetearPassword("token-usado", "NuevaPassword123"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("inválido o ya utilizado");

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(tokenRecuperacionRepo, never()).save(org.mockito.ArgumentMatchers.any());
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.anyString());
    }

    private User usuario() {
        User user = new User();
        user.setCodigoUsua(10L);
        user.setCorreoUsuario("cesar@udistrital.edu.co");
        user.setPrimerNombre("César");
        user.setPasswordUsua("password-anterior");
        return user;
    }
}
