/**
 * Implementación del servicio de administración de usuarios del marketplace UD.
 *
 * <p>Centraliza la lógica de negocio relacionada con consulta administrativa de
 * usuarios y gestión del PDF de autorización de menores de edad.
 *
 * <p>Esta clase actúa como capa intermedia entre el controlador REST y el repositorio,
 * aplicando validaciones, transformaciones a DTO y reglas de negocio antes de acceder
 * a la persistencia.
 *
 * @version 1.0
 * @since 2026-05-29
 */
package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.dto.UserSummaryResponse;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.service.FileValidationService;
import com.udmarketplace.auth.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    /** Repositorio de acceso a datos para usuarios del sistema. */
    private final UserRepository userRepository;

    /** Servicio encargado de validar archivos PDF antes de almacenarlos. */
    private final FileValidationService fileValidationService;

    /**
     * Obtiene un listado resumido de todos los usuarios registrados.
     *
     * @return lista de DTOs con información resumida de usuarios
     */
    @Override
    public List<UserSummaryResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserSummaryResponse.builder()
                        .codigoUsua(user.getCodigoUsua())
                        .correoUsuario(user.getCorreoUsuario())
                        .rolUsua(user.getRolUsua().name())
                        .primerNombre(user.getPrimerNombre())
                        .primerApellido(user.getPrimerApellido())
                        .activo(user.isActivo())
                        .menorEdad(user.isMenorEdad())
                        .build())
                .toList();
    }

    /**
     * Obtiene el detalle básico de un usuario específico.
     *
     * @param userId identificador único del usuario
     * @return DTO con información del usuario
     * @throws RuntimeException si el usuario no existe
     */
    @Override
    public UserInfoResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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

    /**
     * Recupera el PDF de autorización de un usuario menor de edad.
     *
     * @param userId identificador único del usuario
     * @return contenido binario del archivo PDF
     * @throws RuntimeException si el usuario no existe, no es menor o no tiene PDF registrado
     */
    @Override
    public byte[] getPermisoMenorPdf(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.isMenorEdad()) {
            throw new RuntimeException("El usuario no es menor de edad");
        }

        if (user.getPermisoUserMenor() == null || user.getPermisoUserMenor().length == 0) {
            throw new RuntimeException("El usuario no tiene PDF de autorización registrado");
        }

        return user.getPermisoUserMenor();
    }

    /**
     * Actualiza el archivo PDF de autorización de un usuario menor de edad.
     *
     * <p>Antes de persistir el nuevo archivo, se valida formato, tamaño y demás
     * restricciones definidas por la política de carga del sistema.
     *
     * @param userId identificador único del usuario
     * @param pdfAutorizacion nuevo archivo PDF a asociar
     * @throws RuntimeException si el usuario no existe, no es menor o el archivo no puede leerse
     */
    @Override
    public void updatePermisoMenorPdf(Long userId, MultipartFile pdfAutorizacion) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.isMenorEdad()) {
            throw new RuntimeException("Solo los usuarios menores de edad requieren autorización");
        }

        fileValidationService.validatePdf(pdfAutorizacion);

        try {
            user.setPermisoUserMenor(pdfAutorizacion.getBytes());
            userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("No fue posible leer el PDF de autorización", e);
        }
    }
}