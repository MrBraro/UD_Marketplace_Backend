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
 * <p>Responsabilidades:
 * <ol>
 *   <li>Extrae el token del header {@code Authorization: Bearer <token>}</li>
 *   <li>Valida la firma y expiración del JWT ({@link JwtUtil#isTokenValid})</li>
 *   <li>Verifica que el token no esté en la blacklist (RF13)</li>
 *   <li>Si todo es válido, establece la autenticación en el {@link SecurityContextHolder}</li>
 * </ol>
 *
 * <p>Si el token es inválido o está en blacklist, el filtro simplemente no establece
 * autenticación y Spring Security retornará 401 automáticamente.
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
        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

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
