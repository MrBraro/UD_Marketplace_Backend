package com.udmarketplace.auth.config;

import com.udmarketplace.auth.model.Role;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Carga datos iniciales (seed) al arrancar la aplicación alineados al diagrama ER.
 *
 * <p><strong>Credenciales de prueba:</strong>
 * <pre>
 *   ADMIN  → Correo: admin@udmarketplace.com    | Password: Admin123!
 *   SELLER → Correo: seller1@udmarketplace.com  | Password: Seller123!
 *   BUYER  → Correo: buyer1@udmarketplace.com   | Password: Buyer123!
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataSeeder: usuarios ya existen, omitiendo seed.");
            return;
        }

        createUser("admin@udmarketplace.com", "Admin123!", Role.ADMIN, 
                "Carlos", "Augusto", "Pérez", "Gómez", 
                LocalDate.of(1985, 5, 15), "Masculino");

        createUser("seller1@udmarketplace.com", "Seller123!", Role.SELLER, 
                "María", "Isabel", "Rodríguez", "Sánchez", 
                LocalDate.of(1990, 8, 22), "Femenino");

        createUser("buyer1@udmarketplace.com", "Buyer123!", Role.BUYER, 
                "Juan", null, "García", "Martínez", 
                LocalDate.of(1995, 12, 10), "Masculino");

        log.info("DataSeeder: 3 usuarios de prueba creados (admin@udmarketplace.com, seller1@udmarketplace.com, buyer1@udmarketplace.com)");
    }

    private void createUser(String correoUsuario, String rawPassword, Role role,
                            String primerNombre, String segundoNombre,
                            String primerApellido, String segundoApellido,
                            LocalDate fechaNacimiento, String genero) {
        User user = User.builder()
                .correoUsuario(correoUsuario)
                .passwordUsua(passwordEncoder.encode(rawPassword))
                .rolUsua(role)
                .primerNombre(primerNombre)
                .segundoNombre(segundoNombre)
                .primerApellido(primerApellido)
                .segundoApellido(segundoApellido)
                .fechaNacimiento(fechaNacimiento)
                .genero(genero)
                .build();
        userRepository.save(user);
        log.debug("DataSeeder: usuario '{}' creado con rol {}", correoUsuario, role);
    }
}
