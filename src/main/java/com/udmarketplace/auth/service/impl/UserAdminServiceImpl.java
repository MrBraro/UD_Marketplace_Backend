package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.dto.UserSummaryResponse;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;

    @Override
    public List<UserSummaryResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserSummaryResponse.builder()
                        .codigoUsua(user.getCodigoUsua())
                        .correoUsuario(user.getCorreoUsuario())
                        .rolUsua(user.getRolUsua() != null ? user.getRolUsua().name() : null)
                        .primerNombre(user.getPrimerNombre())
                        .primerApellido(user.getPrimerApellido())
                        .activo(user.isActivo())
                        .menorEdad(user.isMenorEdad())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public UserInfoResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        return new UserInfoResponse(
                user.getCodigoUsua(),
                user.getCorreoUsuario(),
                user.getRolUsua() != null ? user.getRolUsua().name() : null,
                user.getPrimerNombre(),
                user.getSegundoNombre(),
                user.getPrimerApellido(),
                user.getSegundoApellido(),
                user.getGenero(),
                user.getFechaNacimiento()
        );
    }

    @Override
    public byte[] getPermisoMenorPdf(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        if (!user.isMenorEdad()) {
            throw new RuntimeException("El usuario no está registrado como menor de edad");
        }

        if (user.getPermisoUserMenor() == null) {
            throw new RuntimeException("El PDF de autorización no se encuentra disponible");
        }

        return user.getPermisoUserMenor();
    }

    @Override
    public void updatePermisoMenorPdf(Long userId, MultipartFile pdfAutorizacion) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        if (!user.isMenorEdad()) {
            throw new RuntimeException("El usuario no está registrado como menor de edad");
        }

        try {
            user.setPermisoUserMenor(pdfAutorizacion.getBytes());
            userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el archivo PDF", e);
        }
    }
}
