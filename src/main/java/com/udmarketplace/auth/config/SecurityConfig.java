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
 * Configuración central de Spring Security.
 *
 * <p>Define:
 * <ul>
 *   <li>Política de sesión stateless (JWT — sin HttpSession)</li>
 *   <li>Rutas públicas y protegidas</li>
 *   <li>Restricciones de acceso por rol (RF24)</li>
 *   <li>Integración del filtro JWT</li>
 *   <li>Respuestas 401/403 en formato JSON consistente</li>
 * </ul>
 *
 * <p>{@code @EnableMethodSecurity} habilita {@code @PreAuthorize} en los controladores.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API REST — sin estado, sin CSRF
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Reglas de autorización por URL
            .authorizeHttpRequests(auth -> auth
                    // Endpoints públicos — no requieren JWT
                    .requestMatchers(
                            "/api/auth/login",
                            "/api/auth/verifyTwoFactor"
                    ).permitAll()

                    // H2 console — solo para desarrollo
                    .requestMatchers("/h2-console/**").permitAll()

                    // Endpoints protegidos por rol (RF24)
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/seller/**").hasRole("SELLER")
                    .requestMatchers("/api/buyer/**").hasRole("BUYER")

                    // Cualquier otro endpoint requiere autenticación
                    .anyRequest().authenticated()
            )

            // Headers para H2 console (iframe) — solo desarrollo
            .headers(headers ->
                    headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

            // Respuesta 401 en JSON cuando no hay autenticación válida
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(String.format(
                                "{\"status\":401,\"message\":\"No autenticado: token ausente o inválido\",\"timestamp\":\"%s\"}",
                                LocalDateTime.now()
                        ));
                    })
                    // Respuesta 403 en JSON cuando el rol es insuficiente
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(String.format(
                                "{\"status\":403,\"message\":\"Acceso denegado: permisos insuficientes\",\"timestamp\":\"%s\"}",
                                LocalDateTime.now()
                        ));
                    })
            )

            // Insertar el filtro JWT antes del filtro de autenticación estándar
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Codificador de contraseñas BCrypt.
     * Usado en {@code AuthServiceImpl} para validar passwords y en {@code DataSeeder} para hashearlas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager expuesto como bean.
     * Disponible para futuros usos si se implementa autenticación programática.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
