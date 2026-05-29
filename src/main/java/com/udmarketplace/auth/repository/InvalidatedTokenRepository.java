/**
 * Repositorio JPA para la entidad {@link com.udmarketplace.auth.model.InvalidatedToken}.
 *
 * <p>Gestiona la lista negra de tokens JWT invalidados por logout. La verificación
 * {@link #existsByToken(String)} es consultada por
 * {@link com.udmarketplace.auth.security.JwtFilter} en cada request autenticado.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.repository;

import com.udmarketplace.auth.model.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {

    /**
     * Verifica si el token dado existe en la lista de tokens invalidados.
     *
     * @param token el JWT completo a verificar
     * @return {@code true} si el token fue invalidado (logout previo)
     */
    boolean existsByToken(String token);
}
