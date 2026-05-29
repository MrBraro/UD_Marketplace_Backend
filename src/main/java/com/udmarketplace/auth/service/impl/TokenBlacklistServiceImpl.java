/**
 * Implementación del servicio de lista negra de tokens JWT del marketplace UD.
 *
 * <p>Persiste los tokens invalidados en la tabla {@code invalidated_tokens} y
 * verifica su presencia antes de autorizar cada request autenticado.
 *
 * <p>Decisión de diseño: usa la base de datos relacional existente para desarrollo.
 * Se puede migrar a Redis (con TTL = expiración del JWT) cambiando únicamente esta
 * implementación sin modificar ningún otro componente.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.model.InvalidatedToken;
import com.udmarketplace.auth.repository.InvalidatedTokenRepository;
import com.udmarketplace.auth.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    /** Repositorio para persistir y consultar tokens invalidados. */
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    /** {@inheritDoc} */
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
