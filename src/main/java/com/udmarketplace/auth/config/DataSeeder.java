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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Componente de inicialización que inserta datos de prueba al arrancar la aplicación.
 *
 * <p>Se ejecuta automáticamente al inicio gracias a {@link CommandLineRunner}.
 * Si ya existen usuarios en la base de datos, el seeder omite la inserción
 * para evitar duplicados en reinicios del servidor.
 *
 * <p>Crea tres usuarios de prueba que cubren todos los roles del sistema:
 * <ul>
 *   <li>{@code ADMINISTRADOR} — gestión del catálogo y PQRs</li>
 *   <li>{@code VENDEDOR}      — publicación y gestión de productos</li>
 *   <li>{@code COMPRADOR}     — navegación, compra y valoración</li>
 * </ul>
 *
 * <p>Todas las contraseñas se almacenan mediante hash bcrypt.
 *
 * @version 1.0
 * @since 2026-05-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    /** Repositorio de usuarios para verificar existencia e insertar datos semilla. */
    private final UserRepository userRepository;

    /** Encoder bcrypt para almacenar contraseñas de forma segura. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Punto de entrada del seeder. Se ejecuta una vez al arrancar la aplicación.
     * Si ya hay usuarios registrados, omite la inserción.
     *
     * @param args argumentos de línea de comandos (no usados)
     */
    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataSeeder: usuarios ya existen, omitiendo seed.");
            return;
        }

        createAdmin("admin@udmarketplace.com", "Admin123!",
                "Carlos", "Augusto", "Pérez", "Gómez",
                LocalDate.of(1985, 5, 15), "Masculino", "3001112233", 1001);

        createSeller("seller1@udmarketplace.com", "Seller123!",
                "María", "Isabel", "Rodríguez", "Sánchez",
                LocalDate.of(1990, 8, 22), "Femenino", "3114445566", new BigDecimal("0.00"));

        createBuyer("buyer1@udmarketplace.com", "Buyer123!",
                "Juan", null, "García", "Martínez",
                LocalDate.of(1995, 12, 10), "Masculino", "3227778899");

        log.info("DataSeeder: 3 usuarios de prueba creados.");
    }

    /**
     * Crea y persiste un usuario con rol {@code ADMINISTRADOR}.
     *
     * @param correo          correo electrónico del administrador
     * @param rawPassword     contraseña en texto plano (se hashea con bcrypt)
     * @param primerNombre    primer nombre
     * @param segundoNombre   segundo nombre (puede ser {@code null})
     * @param primerApellido  primer apellido
     * @param segundoApellido segundo apellido
     * @param fechaNacimiento fecha de nacimiento
     * @param genero          género del usuario
     * @param tel             teléfono de contacto
     * @param numeroContrato  número de contrato único del administrador
     */
    private void createAdmin(String correo, String rawPassword,
                             String primerNombre, String segundoNombre,
                             String primerApellido, String segundoApellido,
                             LocalDate fechaNacimiento, String genero, String tel,
                             Integer numeroContrato) {
        Administrador admin = new Administrador();
        populateUserFields(admin, correo, rawPassword, Role.ADMINISTRADOR,
                primerNombre, segundoNombre, primerApellido, segundoApellido,
                fechaNacimiento, genero, tel);
        admin.setNumeroContrato(numeroContrato);
        userRepository.save(admin);
    }

    /**
     * Crea y persiste un usuario con rol {@code VENDEDOR}.
     *
     * @param correo          correo electrónico del vendedor
     * @param rawPassword     contraseña en texto plano (se hashea con bcrypt)
     * @param primerNombre    primer nombre
     * @param segundoNombre   segundo nombre (puede ser {@code null})
     * @param primerApellido  primer apellido
     * @param segundoApellido segundo apellido
     * @param fechaNacimiento fecha de nacimiento
     * @param genero          género del usuario
     * @param tel             teléfono de contacto
     * @param calificacion    calificación inicial del vendedor (normalmente 0.00)
     */
    private void createSeller(String correo, String rawPassword,
                              String primerNombre, String segundoNombre,
                              String primerApellido, String segundoApellido,
                              LocalDate fechaNacimiento, String genero, String tel,
                              BigDecimal calificacion) {
        Vendedor seller = new Vendedor();
        populateUserFields(seller, correo, rawPassword, Role.VENDEDOR,
                primerNombre, segundoNombre, primerApellido, segundoApellido,
                fechaNacimiento, genero, tel);
        seller.setCalificacion(calificacion);
        userRepository.save(seller);
    }

    /**
     * Crea y persiste un usuario con rol {@code COMPRADOR}.
     *
     * @param correo          correo electrónico del comprador
     * @param rawPassword     contraseña en texto plano (se hashea con bcrypt)
     * @param primerNombre    primer nombre
     * @param segundoNombre   segundo nombre (puede ser {@code null})
     * @param primerApellido  primer apellido
     * @param segundoApellido segundo apellido
     * @param fechaNacimiento fecha de nacimiento
     * @param genero          género del usuario
     * @param tel             teléfono de contacto
     */
    private void createBuyer(String correo, String rawPassword,
                             String primerNombre, String segundoNombre,
                             String primerApellido, String segundoApellido,
                             LocalDate fechaNacimiento, String genero, String tel) {
        Comprador buyer = new Comprador();
        populateUserFields(buyer, correo, rawPassword, Role.COMPRADOR,
                primerNombre, segundoNombre, primerApellido, segundoApellido,
                fechaNacimiento, genero, tel);
        userRepository.save(buyer);
    }

    /**
     * Rellena los campos comunes de la entidad {@link User} para cualquier rol.
     * La contraseña se codifica con bcrypt antes de asignarla.
     *
     * @param user            entidad de usuario a poblar (ya instanciada por el subtipo)
     * @param correo          correo electrónico institucional
     * @param rawPassword     contraseña en texto plano
     * @param role            rol asignado al usuario
     * @param primerNombre    primer nombre
     * @param segundoNombre   segundo nombre (puede ser {@code null})
     * @param primerApellido  primer apellido
     * @param segundoApellido segundo apellido
     * @param fechaNacimiento fecha de nacimiento
     * @param genero          género del usuario
     * @param tel             teléfono de contacto
     */
    private void populateUserFields(User user, String correo, String rawPassword, Role role,
                                    String primerNombre, String segundoNombre,
                                    String primerApellido, String segundoApellido,
                                    LocalDate fechaNacimiento, String genero, String tel) {
        user.setCorreoUsuario(correo);
        user.setPasswordUsua(passwordEncoder.encode(rawPassword));
        user.setRolUsua(role);
        user.setPrimerNombre(primerNombre);
        user.setSegundoNombre(segundoNombre);
        user.setPrimerApellido(primerApellido);
        user.setSegundoApellido(segundoApellido);
        user.setFechaNacimiento(fechaNacimiento);
        user.setGenero(genero);
        user.setTelUser(tel);
        user.setActivo(true);
    }
}
