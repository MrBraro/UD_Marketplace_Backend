package com.udmarketplace.auth.security;

import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de {@link UserDetailsService} requerida por Spring Security.
 *
 * <p>Carga el usuario desde la base de datos por su username y lo convierte
 * en un {@link UserDetails} que Spring Security usa internamente para
 * autenticación y autorización.
 *
 * <p>El prefijo "ROLE_" es añadido automáticamente por {@code .roles()} del builder,
 * lo que hace compatible {@code hasRole('ADMIN')} en Spring Security con el enum
 * {@code Role.ADMIN} almacenado en la entidad.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())   // agrega prefijo ROLE_ automáticamente
                .build();
    }
}
