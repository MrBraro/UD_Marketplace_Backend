package com.udmarketplace.auth.config;

import com.udmarketplace.auth.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

/**
 * Configuración central de Spring Security para el sistema UD Marketplace.
 *
 * <p>Establece la política de seguridad de la API REST con las siguientes características:
 * <ul>
 *   <li>Sesiones <b>stateless</b> (JWT, sin HttpSession)</li>
 *   <li>CSRF deshabilitado (no aplica a APIs REST sin formularios)</li>
 *   <li>Seguridad a nivel de método habilitada con {@link EnableMethodSecurity},
 *       permitiendo el uso de {@code @PreAuthorize}</li>
 *   <li>Exposición del bean {@link PasswordEncoder} usando BCrypt para hash seguro
 *       de contraseñas, evitando almacenamiento en texto plano</li>
 *   <li>RBAC mediante {@code @PreAuthorize} habilitado con {@code @EnableMethodSecurity}</li>
 *   <li>Endpoints públicos: login, 2FA, recuperación de contraseña, catálogo y valoraciones</li>
 *   <li>Prefijos de ruta protegidos por rol: {@code /api/admin/**}, {@code /api/seller/**}, {@code /api/buyer/**}</li>
 *   <li>Respuestas 401/403 en formato JSON consistente con {@link com.udmarketplace.auth.dto.ErrorResponse}</li>
 * </ul>
 *
 * <p>El {@link JwtFilter} se inserta antes de {@link UsernamePasswordAuthenticationFilter}
 * para validar el token en cada solicitud protegida.
 *
 * @version 1.1
 * @since 2026-05-28
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** Filtro JWT que valida el token en cada solicitud entrante. */
    private final JwtFilter jwtFilter;

    /**
     * Define la cadena de filtros de seguridad con las reglas de autorización por rol y endpoint.
     *
     * <p>Se permite acceso público a los endpoints de autenticación inicial,
     * registro, recuperación de contraseña, catálogo y valoraciones. El resto
     * de endpoints requiere autenticación, y algunos prefijos además exigen
     * un rol específico.
     *
     * @param http builder de configuración de seguridad HTTP
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si la configuración de Spring Security falla
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/api/auth/register",
                            "/api/auth/login",
                            "/api/auth/verifyTwoFactor",
                            "/api/auth/recuperar-password",
                            "/api/auth/reset-password"
                    ).permitAll()

                    // Endpoints públicos de catálogo y valoraciones
                    .requestMatchers(
                            "/api/categorias",
                            "/api/categorias/**",
                            "/api/productos",
                            "/api/productos/**",
                            "/api/valoraciones/**"
                    ).permitAll()

                    // Endpoints protegidos por rol
                    .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                    .requestMatchers("/api/seller/**").hasRole("VENDEDOR")
                    .requestMatchers("/api/buyer/**").hasRole("COMPRADOR")

                    .anyRequest().authenticated()
            )

            .headers(headers ->
                    headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(String.format(
                                "{\"status\":401,\"message\":\"No autenticado: token ausente o inválido\",\"timestamp\":\"%s\"}",
                                LocalDateTime.now()
                        ));
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(String.format(
                                "{\"status\":403,\"message\":\"Acceso denegado: permisos insuficientes\",\"timestamp\":\"%s\"}",
                                LocalDateTime.now()
                        ));
                    })
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Registra BCrypt como algoritmo de hash para contraseñas (factor de coste 10 por defecto).
     *
     * @return encoder de contraseñas basado en BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el {@link AuthenticationManager} de Spring para ser inyectado en servicios que
     * requieran autenticación programática.
     *
     * @param config configuración de autenticación de Spring Security
     * @return instancia del gestor de autenticación
     * @throws Exception si la obtención del manager falla
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
