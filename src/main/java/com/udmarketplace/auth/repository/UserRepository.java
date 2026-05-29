/**
 * Repositorio JPA para la entidad {@link com.udmarketplace.auth.model.User}.
 *
 * <p>Provee operaciones CRUD estándar heredadas de {@link org.springframework.data.jpa.repository.JpaRepository}
 * más la búsqueda por {@code correo_usuario}, que es el identificador único de acceso
 * definido en el diagrama ER. Usado por todos los módulos para recuperar usuarios por su código.
 * 
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.repository;

import com.udmarketplace.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su correo electrónico (identificador único de acceso).
     *
     * @param correoUsuario el correo electrónico del usuario
     * @return {@link Optional} con el usuario si existe, vacío en caso contrario
     */
    Optional<User> findByCorreoUsuario(String correoUsuario);
}
