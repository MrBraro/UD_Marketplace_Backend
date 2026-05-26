package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.model.InvalidatedToken;
import com.udmarketplace.auth.repository.InvalidatedTokenRepository;
import com.udmarketplace.auth.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementación del servicio de blacklist de tokens JWT (RF13).
 *
 * <p>Persiste los tokens invalidados en la tabla {@code invalidated_tokens}
 * y verifica su presencia antes de autorizar cada request.
 *
 * <p>Decisión de diseño: usa la base de datos relacional existente.
 * Se puede migrar a Redis (con TTL = expiración del JWT) cambiando
 * únicamente esta implementación sin tocar ningún otro componente.
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public void invalidateToken(String token) {
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .token(token)
                .invalidatedAt(LocalDateTime.now())
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
    }

    @Override
    public boolean isTokenInvalidated(String token) {
        return invalidatedTokenRepository.existsByToken(token);
    }
}
