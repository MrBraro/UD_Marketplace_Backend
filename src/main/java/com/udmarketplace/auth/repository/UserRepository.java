package com.udmarketplace.auth.repository;

import com.udmarketplace.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link User}.
 *
 * <p>Provee operaciones CRUD estándar heredadas de {@link JpaRepository}
 * más la búsqueda por username necesaria para el proceso de autenticación.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario (identificador de acceso).
     *
     * @param username el identificador único de acceso
     * @return {@link Optional} con el usuario si existe, vacío en caso contrario
     */
    Optional<User> findByUsername(String username);
}
