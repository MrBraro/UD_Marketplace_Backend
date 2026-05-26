package com.udmarketplace.auth.config;

import com.udmarketplace.auth.model.Administrador;
import com.udmarketplace.auth.model.Comprador;
import com.udmarketplace.auth.model.Role;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.model.Vendedor;
import com.udmarketplace.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Carga datos iniciales (seed) al arrancar la aplicación.
 *
 * <p>Los usuarios de prueba se crean con todos los campos del diccionario técnico:
 * <pre>
 *   ADMINISTRADOR → correo_institu: admin@udmarketplace.com    | password: Admin123!
 *   VENDEDOR      → correo_institu: seller1@udmarketplace.com  | password: Seller123!
 *   COMPRADOR     → correo_institu: buyer1@udmarketplace.com   | password: Buyer123!
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

        createAdmin("admin@udmarketplace.com", "Admin123!",
                "Carlos", "Augusto", "Pérez", "Gómez",
                LocalDate.of(1985, 5, 15), "Masculino", "3001112233", "C-ADMIN-001");

        createSeller("seller1@udmarketplace.com", "Seller123!",
                "María", "Isabel", "Rodríguez", "Sánchez",
                LocalDate.of(1990, 8, 22), "Femenino", "3114445566", new java.math.BigDecimal("4.50"));

        createBuyer("buyer1@udmarketplace.com", "Buyer123!",
                "Juan", null, "García", "Martínez",
                LocalDate.of(1995, 12, 10), "Masculino", "3227778899");

        log.info("DataSeeder: 3 usuarios de prueba (Administrador, Vendedor, Comprador) creados correctamente.");
    }

    private void createAdmin(String correoInstitu, String rawPassword,
                             String primerNombre, String segundoNombre,
                             String primerApellido, String segundoApellido,
                             LocalDate fechaNacimiento, String genero, String telUser,
                             String numeroContrato) {
        Administrador admin = new Administrador();
        populateUserFields(admin, correoInstitu, rawPassword, Role.ADMINISTRADOR,
                primerNombre, segundoNombre, primerApellido, segundoApellido,
                fechaNacimiento, genero, telUser);
        admin.setNumeroContrato(numeroContrato);
        userRepository.save(admin);
        log.debug("DataSeeder: Administrador '{}' creado.", correoInstitu);
    }

    private void createSeller(String correoInstitu, String rawPassword,
                              String primerNombre, String segundoNombre,
                              String primerApellido, String segundoApellido,
                              LocalDate fechaNacimiento, String genero, String telUser,
                              java.math.BigDecimal calificacion) {
        Vendedor seller = new Vendedor();
        populateUserFields(seller, correoInstitu, rawPassword, Role.VENDEDOR,
                primerNombre, segundoNombre, primerApellido, segundoApellido,
                fechaNacimiento, genero, telUser);
        seller.setCalificacion(calificacion);
        userRepository.save(seller);
        log.debug("DataSeeder: Vendedor '{}' creado.", correoInstitu);
    }

    private void createBuyer(String correoInstitu, String rawPassword,
                             String primerNombre, String segundoNombre,
                             String primerApellido, String segundoApellido,
                             LocalDate fechaNacimiento, String genero, String telUser) {
        Comprador buyer = new Comprador();
        populateUserFields(buyer, correoInstitu, rawPassword, Role.COMPRADOR,
                primerNombre, segundoNombre, primerApellido, segundoApellido,
                fechaNacimiento, genero, telUser);
        userRepository.save(buyer);
        log.debug("DataSeeder: Comprador '{}' creado.", correoInstitu);
    }

    private void populateUserFields(User user, String correoInstitu, String rawPassword, Role role,
                                    String primerNombre, String segundoNombre,
                                    String primerApellido, String segundoApellido,
                                    LocalDate fechaNacimiento, String genero, String telUser) {
        user.setCorreoInstitu(correoInstitu);
        user.setPasswordUsua(passwordEncoder.encode(rawPassword));
        user.setPerimisoUser(role);
        user.setPrimerNombre(primerNombre);
        user.setSegundoNombre(segundoNombre);
        user.setPrimerApellido(primerApellido);
        user.setSegundoApellido(segundoApellido);
        user.setFechaNacimiento(fechaNacimiento);
        user.setGenero(genero);
        user.setTelUser(telUser);
        user.setActivo(true);
    }
}
