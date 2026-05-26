package com.udmarketplace.auth.repository;

import com.udmarketplace.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link User}.
 *
 * <p>Provee operaciones CRUD estándar heredadas de {@link JpaRepository}
 * más la búsqueda por correo_usuario (identificador de acceso único del diagrama ER).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su correo electrónico (identificador único de acceso).
     *
     * @param correoUsuario el correo electrónico del usuario
     * @return {@link Optional} con el usuario si existe, vacío en caso contrario
     */
    Optional<User> findByCorreoUsuario(String correoUsuario);
}
