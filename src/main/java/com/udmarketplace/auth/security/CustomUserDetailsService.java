/**
 * Implementación de {@link org.springframework.security.core.userdetails.UserDetailsService}
 * requerida por Spring Security para el marketplace UD.
 *
 * <p>Carga el usuario por su {@code correo_usuario} (identificador único según el diagrama ER)
 * y construye el objeto {@code UserDetails} con rol prefijado {@code ROLE_} que Spring Security
 * usa para evaluar las anotaciones {@code @PreAuthorize}.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.security;

import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /** Repositorio de usuarios para la búsqueda por correo electrónico. */
    private final UserRepository userRepository;

    /**
     * Carga el usuario identificado por su correo electrónico y lo adapta al contrato
     * de Spring Security. Agrega el prefijo {@code ROLE_} al rol del usuario automáticamente.
     *
     * @param correoUsuario correo electrónico del usuario (campo {@code correo_usuario})
     * @return {@link UserDetails} con credenciales y rol del usuario
     * @throws UsernameNotFoundException si no existe un usuario con el correo indicado
     */
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
