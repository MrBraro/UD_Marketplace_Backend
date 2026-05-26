package com.udmarketplace.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para generación y validación de tokens JWT (RF11, RF13).
 *
 * <p>Usa el algoritmo HS256 con una clave simétrica configurada.
 *
 * <p>Claims del JWT:
 * <ul>
 *   <li>{@code sub}  — correoUsuario del usuario (correo_usuario en ER)</li>
 *   <li>{@code role} — rol del usuario (rol_usua en ER: ADMIN, SELLER, BUYER)</li>
 *   <li>{@code iat}  — timestamp de emisión</li>
 *   <li>{@code exp}  — timestamp de expiración (iat + 24h)</li>
 * </ul>
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Genera un JWT firmado con HS256.
     *
     * @param correoUsuario correo del usuario a incluir como subject
     * @param rolUsua       rol del usuario a incluir como claim
     * @return JWT compacto listo para enviar al cliente
     */
    public String generateToken(String correoUsuario, String rolUsua) {
        return Jwts.builder()
                .subject(correoUsuario)
                .claim("role", rolUsua)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
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

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
