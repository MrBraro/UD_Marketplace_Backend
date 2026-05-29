package com.udmarketplace.auth.repository;

import com.udmarketplace.auth.model.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link TokenRecuperacion}.
 *
 * <p>Permite consultar y persistir los tokens de recuperación de contraseña.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    /**
     * Busca un token de recuperación válido (no usado) por su valor.
     *
     * @param token valor UUID del token
     * @return {@link Optional} con el token si existe y no ha sido usado
     */
    Optional<TokenRecuperacion> findByTokenAndUsadoFalse(String token);
}
