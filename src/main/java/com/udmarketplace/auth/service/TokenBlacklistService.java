/**
 * Contrato del servicio de lista negra de tokens JWT del marketplace UD.
 *
 * <p>Gestiona la invalidación de tokens al hacer logout y su verificación
 * en cada request autenticado para impedir el reuso de tokens cerrados.
 * La implementación actual persiste en MySQL; puede migrarse a Redis con TTL
 * sin cambiar ningún otro componente.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.service;

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
