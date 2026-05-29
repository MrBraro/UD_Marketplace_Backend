/**
 * Mapper de entidades de usuario a DTOs de respuesta del marketplace UD.
 *
 * <p>Convierte la entidad {@link com.udmarketplace.auth.model.User} a su representación
 * segura de respuesta, excluyendo datos sensibles como contraseña y código 2FA.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.mapper;

import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * Convierte un {@link User} en un {@link UserInfoResponse} alineado al diagrama ER.
     * Expone únicamente los campos del perfil del usuario, sin información sensible.
     *
     * @param user entidad del usuario a convertir
     * @return DTO con todos los campos del perfil especificados en el diagrama ER
     */
    public UserInfoResponse toUserInfoResponse(User user) {
        return new UserInfoResponse(
                user.getCodigoUsua(),
                user.getCorreoUsuario(),
                user.getRolUsua().name(),
                user.getPrimerNombre(),
                user.getSegundoNombre(),
                user.getPrimerApellido(),
                user.getSegundoApellido(),
                user.getGenero(),
                user.getFechaNacimiento()
        );
    }
}
