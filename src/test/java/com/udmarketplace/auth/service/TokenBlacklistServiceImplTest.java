package com.udmarketplace.auth.service;

import com.udmarketplace.auth.model.InvalidatedToken;
import com.udmarketplace.auth.repository.InvalidatedTokenRepository;
import com.udmarketplace.auth.service.impl.TokenBlacklistServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para la lista negra de tokens JWT.
 *
 * <p>Cubre RNF05 para tokens de sesión: un token invalidado por logout
 * debe persistirse y luego debe ser reconocido como no reutilizable.
 */
@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceImplTest {

    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @InjectMocks
    private TokenBlacklistServiceImpl service;

    @Test
    void invalidateToken_debeGuardarTokenConFechaInvalidacion() {
        String token = "jwt-token-test";

        service.invalidateToken(token);

        ArgumentCaptor<InvalidatedToken> captor = ArgumentCaptor.forClass(InvalidatedToken.class);
        verify(invalidatedTokenRepository).save(captor.capture());

        InvalidatedToken guardado = captor.getValue();

        assertThat(guardado.getToken()).isEqualTo(token);
        assertThat(guardado.getInvalidatedAt()).isNotNull();
        assertThat(guardado.getInvalidatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void isTokenInvalidated_debeRetornarTrueCuandoTokenExisteEnBlacklist() {
        String token = "jwt-token-invalidado";

        when(invalidatedTokenRepository.existsByToken(token)).thenReturn(true);

        boolean result = service.isTokenInvalidated(token);

        assertThat(result).isTrue();
        verify(invalidatedTokenRepository).existsByToken(token);
    }

    @Test
    void isTokenInvalidated_debeRetornarFalseCuandoTokenNoExisteEnBlacklist() {
        String token = "jwt-token-activo";

        when(invalidatedTokenRepository.existsByToken(token)).thenReturn(false);

        boolean result = service.isTokenInvalidated(token);

        assertThat(result).isFalse();
        verify(invalidatedTokenRepository).existsByToken(token);
    }
}
