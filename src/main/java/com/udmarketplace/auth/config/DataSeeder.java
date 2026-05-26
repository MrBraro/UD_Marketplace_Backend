package com.udmarketplace.auth.config;

import com.udmarketplace.auth.model.Role;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga datos iniciales (seed) al arrancar la aplicación.
 *
 * <p>Crea usuarios de prueba para los tres roles disponibles solo si la
 * tabla de usuarios está vacía. Esto evita duplicados en reinicios.
 *
 * <p>Decisión de diseño: los usuarios se crean aquí temporalmente para permitir
 * pruebas del flujo de autenticación y autorización. El registro de usuarios
 * será implementado por el equipo correspondiente en una iteración posterior.
 *
 * <p><strong>Credenciales de prueba:</strong>
 * <pre>
 *   ADMIN  → username: admin    | password: Admin123!    | email: admin@udmarketplace.com
 *   SELLER → username: seller1  | password: Seller123!   | email: seller1@udmarketplace.com
 *   BUYER  → username: buyer1   | password: Buyer123!    | email: buyer1@udmarketplace.com
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

        createUser("admin",   "admin@udmarketplace.com",   "Admin123!",   Role.ADMIN);
        createUser("seller1", "seller1@udmarketplace.com", "Seller123!",  Role.SELLER);
        createUser("buyer1",  "buyer1@udmarketplace.com",  "Buyer123!",   Role.BUYER);

        log.info("DataSeeder: 3 usuarios de prueba creados (admin, seller1, buyer1)");
    }

    private void createUser(String username, String email, String rawPassword, Role role) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();
        userRepository.save(user);
        log.debug("DataSeeder: usuario '{}' creado con rol {}", username, role);
    }
}
