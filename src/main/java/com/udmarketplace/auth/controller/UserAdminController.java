/**
 * Controlador REST de administración de usuarios del marketplace UD.
 *
 * <p>Expone endpoints protegidos para consulta y gestión administrativa de usuarios,
 * restringidos al rol {@code ADMINISTRADOR}. Permite:
 * <ul>
 *   <li>Listar usuarios registrados</li>
 *   <li>Consultar el detalle básico de un usuario específico</li>
 *   <li>Descargar el PDF de autorización de un usuario menor de edad</li>
 *   <li>Actualizar el PDF de autorización de un usuario menor de edad</li>
 * </ul>
 *
 * <p>Este controlador forma parte del módulo de gestión de usuarios, separado del
 * controlador de autenticación para mantener una organización por responsabilidad
 * funcional dentro del backend.
 *
 * @version 1.0
 * @since 2026-05-29
 */
package com.udmarketplace.auth.controller;

import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.dto.UserSummaryResponse;
import com.udmarketplace.auth.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    /** Servicio de administración de usuarios que encapsula la lógica de negocio. */
    private final UserAdminService userAdminService;

    /**
     * Lista todos los usuarios registrados en el sistema.
     *
     * <p>Retorna una vista resumida de cada usuario para facilitar su visualización
     * en paneles administrativos o tablas de gestión.
     *
     * @return lista de usuarios resumidos
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UserSummaryResponse>> listUsers() {
        return ResponseEntity.ok(userAdminService.listUsers());
    }

    /**
     * Consulta el detalle básico de un usuario por su identificador.
     *
     * <p>La respuesta excluye información sensible como contraseña o códigos 2FA.
     *
     * @param id identificador único del usuario
     * @return detalle del usuario solicitado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UserInfoResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userAdminService.getUserById(id));
    }

    /**
     * Descarga o visualiza el archivo PDF de autorización asociado a un usuario menor de edad.
     *
     * <p>El archivo se retorna como {@code application/pdf}. Si el usuario no es menor
     * o no tiene autorización registrada, la capa de servicio lanza la excepción correspondiente.
     *
     * @param id identificador único del usuario
     * @return archivo PDF de autorización
     */
    @GetMapping("/{id}/permiso-menor")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> downloadPermisoMenor(@PathVariable Long id) {
        byte[] pdf = userAdminService.getPermisoMenorPdf(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=permiso_menor_user_" + id + ".pdf")
                .body(pdf);
    }
    /**
     * Reemplaza el archivo PDF de autorización de un usuario menor de edad.
     *
     * <p>El archivo recibido se valida antes de almacenarse nuevamente en la base de datos.
     *
     * @param id identificador único del usuario
     * @param pdfAutorizacion nuevo archivo PDF de autorización
     * @return mensaje de confirmación de actualización exitosa
     */
    @PutMapping(value = "/{id}/permiso-menor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<String> updatePermisoMenor(
            @PathVariable Long id,
            @RequestPart("pdfAutorizacion") MultipartFile pdfAutorizacion) {

        userAdminService.updatePermisoMenorPdf(id, pdfAutorizacion);
        return ResponseEntity.ok("PDF de autorización actualizado correctamente");
    }
}