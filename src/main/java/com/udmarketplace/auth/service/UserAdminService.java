/**
 * Contrato de servicio para la administración de usuarios del marketplace UD.
 *
 * <p>Define las operaciones disponibles para consulta administrativa de usuarios
 * y gestión del archivo PDF de autorización requerido para menores de edad.
 *
 * <p>La implementación de este servicio debe encargarse de aplicar las reglas
 * de negocio correspondientes y coordinar el acceso al repositorio de usuarios.
 *
 * @version 1.0
 * @since 2026-05-29
 */
package com.udmarketplace.auth.service;

import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.dto.UserSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserAdminService {

    /**
     * Obtiene el listado resumido de usuarios registrados.
     *
     * @return lista de usuarios resumidos
     */
    List<UserSummaryResponse> listUsers();

    /**
     * Obtiene el detalle básico de un usuario por su identificador.
     *
     * @param userId identificador único del usuario
     * @return información del usuario
     */
    UserInfoResponse getUserById(Long userId);

    /**
     * Obtiene el archivo PDF de autorización asociado a un usuario menor de edad.
     *
     * @param userId identificador único del usuario
     * @return contenido binario del PDF
     */
    byte[] getPermisoMenorPdf(Long userId);

    /**
     * Actualiza el archivo PDF de autorización asociado a un usuario menor de edad.
     *
     * @param userId identificador único del usuario
     * @param pdfAutorizacion nuevo archivo PDF de autorización
     */
    void updatePermisoMenorPdf(Long userId, MultipartFile pdfAutorizacion);
}