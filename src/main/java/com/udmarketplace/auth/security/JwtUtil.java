package com.udmarketplace.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para la generación, validación y extracción de claims en tokens JWT.
 *
 * <p>Utiliza el algoritmo HMAC-SHA256 (HS256) con una clave simétrica configurada
 * mediante la propiedad {@code app.jwt.secret}.
 *
 * <p>Claims incluidos en cada JWT:
 * <ul>
 *   <li>{@code sub}    — correoUsuario del usuario autenticado</li>
 *   <li>{@code role}   — rol del usuario ({@code ADMINISTRADOR}, {@code VENDEDOR}, {@code COMPRADOR})</li>
 *   <li>{@code userId} — identificador único del usuario (asociar token al usuario)</li>
 *   <li>{@code iat}    — timestamp de emisión</li>
 *   <li>{@code exp}    — timestamp de expiración (configurable, por defecto 24 h)</li>
 * </ul>
 *
 * <p>El tiempo de expiración se configura mediante {@code app.jwt.expiration-ms}.
 *
 * @version 1.1
 * @since 2026-05-28
 */
@Component
public class JwtUtil {

    /** Clave secreta HMAC-SHA256 para firmar y verificar los tokens. */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Tiempo de vida del token en milisegundos (configurable, por defecto 24 h). */
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Genera un JWT firmado con HS256.
     *
     * @param correoUsuario correo del usuario (subject)
     * @param rolUsua       rol del usuario
     * @param userId        id del usuario (REQ-01: asociar token al usuario)
     * @return JWT compacto listo para enviar al cliente
     */
    public String generateToken(String correoUsuario, String rolUsua, Long userId) {
        return Jwts.builder()
                .subject(correoUsuario)
                .claim("role", rolUsua)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el identificador único del usuario desde el claim {@code userId} del token (REQ-01).
     *
     * <p>Maneja tanto el caso en que el claim se deserialice como {@code Integer}
     * (comportamiento por defecto de Jackson para valores pequeños) como {@code Long}.
     *
     * @param token JWT compacto
     * @return identificador del usuario, o {@code null} si el claim no existe
     */
    public Long extractUserId(String token) {
        Object id = parseClaims(token).get("userId");
        if (id instanceof Integer) return ((Integer) id).longValue();
        if (id instanceof Long) return (Long) id;
        return null;
    }

    /**
     * Extrae el correoUsuario (subject) de un token JWT.
     *
     * @param token JWT compacto
     * @return correoUsuario almacenado en el subject del token
     */
    public String extractCorreoUsuario(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae el rol del usuario desde el claim "role" del token.
     *
     * @param token JWT compacto
     * @return rol del usuario (ej: "ADMIN")
     */
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Verifica si un token JWT es estructuralmente válido y no está expirado.
     *
     * @param token JWT compacto
     * @return {@code true} si el token es válido y no expiró
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parsea y verifica la firma del JWT, retornando los claims del payload.
     *
     * @param token JWT compacto
     * @return claims del payload
     * @throws io.jsonwebtoken.JwtException si el token es inválido o la firma no coincide
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Construye la clave HMAC-SHA256 a partir del secreto configurado.
     *
     * @return clave criptográfica para firma y verificación de tokens
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
