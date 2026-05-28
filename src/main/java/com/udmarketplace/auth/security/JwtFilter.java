/**
 * Filtro de autenticación JWT del marketplace UD. Se ejecuta una única vez por request HTTP.
 *
 * <p>Ejecuta la siguiente secuencia de validación por cada request:
 * <ol>
 *   <li>Extrae el token Bearer del header {@code Authorization}.</li>
 *   <li>Valida la estructura, firma y vigencia del token con {@link JwtUtil}.</li>
 *   <li>Verifica que el token no fue invalidado por logout ({@link TokenBlacklistService}).</li>
 *   <li>Si todo es válido, establece la autenticación en el {@code SecurityContextHolder}.</li>
 * </ol>
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.security;

import com.udmarketplace.auth.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    /** Utilidad JWT para validación y extracción de claims. */
    private final JwtUtil jwtUtil;

    /** Servicio de carga de usuarios requerido por Spring Security. */
    private final CustomUserDetailsService userDetailsService;

    /** Servicio de lista negra para rechazar tokens invalidados por logout. */
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Validación estructural y de firma/expiración
        if (!jwtUtil.isTokenValid(token)) {
            log.debug("JWT inválido o expirado en request a: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Verificar blacklist (token invalidado por logout)
        if (tokenBlacklistService.isTokenInvalidated(token)) {
            log.debug("Token en blacklist (sesión cerrada) en request a: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Token válido — establecer autenticación en el contexto de seguridad
        String correoUsuario = jwtUtil.extractCorreoUsuario(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(correoUsuario);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
