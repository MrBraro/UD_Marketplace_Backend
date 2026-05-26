package com.udmarketplace.auth.mapper;

import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades {@link User} en DTOs de respuesta.
 */
@Component
public class UserMapper {

    /**
     * Convierte un {@link User} en un {@link UserInfoResponse} alineado al diagrama ER.
     *
     * @param user entidad del usuario
     * @return DTO seguro con todos los campos especificados en el diagrama ER
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
