package com.udmarketplace.auth.service;

/**
 * Contrato del servicio de blacklist de tokens JWT.
 *
 * <p>Gestiona la invalidación de tokens en logout (RF13)
 * y la verificación en cada request autenticado.
 */
public interface TokenBlacklistService {

    /**
     * Persiste el token como invalidado. Llamado durante el logout.
     *
     * @param token JWT a invalidar
     */
    void invalidateToken(String token);

    /**
     * Verifica si el token fue previamente invalidado.
     * Consultado por {@code JwtFilter} en cada request protegido.
     *
     * @param token JWT a verificar
     * @return {@code true} si el token fue invalidado (sesión cerrada)
     */
    boolean isTokenInvalidated(String token);
}
