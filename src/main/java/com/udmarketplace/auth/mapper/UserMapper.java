package com.udmarketplace.auth.mapper;

import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades {@link User} en DTOs de respuesta.
 *
 * <p>Centraliza la transformación entity → DTO evitando que los controladores
 * o servicios accedan directamente a los campos de la entidad.
 * Garantiza que datos sensibles (password, twoFactorCode) nunca se expongan.
 */
@Component
public class UserMapper {

    /**
     * Convierte un {@link User} en un {@link UserInfoResponse}.
     *
     * @param user entidad del usuario
     * @return DTO seguro para enviar al cliente (sin datos sensibles)
     */
    public UserInfoResponse toUserInfoResponse(User user) {
        return new UserInfoResponse(
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
