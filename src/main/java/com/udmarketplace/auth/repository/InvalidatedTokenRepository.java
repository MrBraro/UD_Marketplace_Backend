package com.udmarketplace.auth.repository;

import com.udmarketplace.auth.model.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad {@link InvalidatedToken}.
 *
 * <p>Permite verificar si un token fue previamente invalidado (blacklist),
 * operación ejecutada en cada request autenticado por el filtro JWT.
 */
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {

    /**
     * Verifica si el token dado existe en la lista de tokens invalidados.
     *
     * @param token el JWT completo a verificar
     * @return {@code true} si el token fue invalidado (logout previo)
     */
    boolean existsByToken(String token);
}
