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

/**
 * Filtro JWT que se ejecuta una vez por cada request HTTP.
 *
 * <p>Extrae y valida el token usando correo_usuario como identificador principal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
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
