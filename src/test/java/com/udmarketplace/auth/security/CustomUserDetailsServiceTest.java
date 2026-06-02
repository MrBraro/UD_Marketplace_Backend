package com.udmarketplace.auth.security;

import com.udmarketplace.auth.model.EstadoCuenta;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsServiceTest")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_usuarioSuspendido_lanzaExcepcion() {
        User user = new User();
        user.setCorreoUsuario("suspendido@udistrital.edu.co");
        user.setPasswordUsua("hash");
        user.setActivo(false);
        user.setEstadoCuenta(EstadoCuenta.SUSPENDIDA);

        when(userRepository.findByCorreoUsuario("suspendido@udistrital.edu.co"))
                .thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("suspendido@udistrital.edu.co"));
    }
}