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
 * <p>Alineado al diagrama ER: busca y carga usuarios por su correo_usuario.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String correoUsuario) throws UsernameNotFoundException {
        User user = userRepository.findByCorreoUsuario(correoUsuario)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con correo: " + correoUsuario));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getCorreoUsuario())
                .password(user.getPasswordUsua())
                .roles(user.getRolUsua().name())   // agrega prefijo ROLE_ automáticamente
                .build();
    }
}
