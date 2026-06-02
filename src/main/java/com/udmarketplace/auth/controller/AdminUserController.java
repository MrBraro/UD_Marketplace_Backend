package com.udmarketplace.auth.controller;

import com.udmarketplace.auth.dto.UpdateUserRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.mapper.UserMapper;
import com.udmarketplace.auth.model.AccionAuditoria;
import com.udmarketplace.auth.model.EstadoCuenta;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.security.JwtUtil;
import com.udmarketplace.auth.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Controlador REST para la administración de usuarios del sistema UD Marketplace.
 *
 * <p>Acceso exclusivo para administradores. Permite:
 * <ul>
 *   <li>{@code GET  /api/admin/usuarios}       — listar todos los usuarios</li>
 *   <li>{@code GET  /api/admin/usuarios/{id}}   — obtener usuario por ID</li>
 *   <li>{@code PUT  /api/admin/usuarios/{id}}   — actualizar datos de un usuario</li>
 * </ul>
 *
 * <p>Cada actualización queda registrada en el log de auditoría (REQ-06).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-06-01
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
@Tag(name = "Administración de Usuarios", description = "Endpoints para la gestión administrativa de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    /** Repositorio de usuarios para operaciones CRUD. */
    private final UserRepository userRepository;

    /** Mapper de entidades a DTOs de respuesta. */
    private final UserMapper userMapper;

    /** Servicio de auditoría para registrar cambios en usuarios. */
    private final AuditService auditService;

    /** Utilidad JWT para extraer datos del administrador autenticado. */
    private final JwtUtil jwtUtil;

    /**
     * Lista todos los usuarios registrados en el sistema.
     *
     * @return lista de usuarios en formato DTO sin datos sensibles
     */
    @GetMapping
    @Operation(
            summary = "Listar todos los usuarios",
            description = "Retorna una lista completa de todos los usuarios registrados en el sistema. Solo accesible para administradores.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - Permisos insuficientes")
            }
    )
    public ResponseEntity<List<UserInfoResponse>> listarUsuarios() {
        List<UserInfoResponse> usuarios = userRepository.findAll()
                .stream()
                .map(userMapper::toUserInfoResponse)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene los datos de un usuario específico por su código.
     *
     * @param id código del usuario a consultar
     * @return datos del usuario en formato DTO
     * @throws RecursoNoEncontradoException si el usuario no existe
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener detalle de usuario",
            description = "Retorna la información de perfil detallada de un usuario por su código. Solo accesible para administradores.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado y detalles retornados"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - Permisos insuficientes"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            }
    )
    public ResponseEntity<UserInfoResponse> obtenerUsuario(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        return ResponseEntity.ok(userMapper.toUserInfoResponse(user));
    }

    /**
     * Actualiza los datos de un usuario existente. Solo se modifican
     * los campos que se envían con valor no nulo (actualización parcial).
     *
     * <p>Registra la operación en el log de auditoría con los campos modificados.
     *
     * @param id         código del usuario a actualizar
     * @param request    DTO con los campos a modificar
     * @param authHeader header Authorization con el token Bearer del administrador
     * @return datos actualizados del usuario
     * @throws RecursoNoEncontradoException si el usuario no existe
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar información de usuario",
            description = "Permite a un administrador actualizar de manera parcial o total la información de un usuario. Registra un evento en auditoría.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido - Permisos insuficientes"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            }
    )
    public ResponseEntity<UserInfoResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader("Authorization") String authHeader) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));

        StringBuilder cambios = new StringBuilder();

        if (request.getPrimerNombre() != null) {
            cambios.append("primerNombre: '").append(user.getPrimerNombre())
                    .append("' → '").append(request.getPrimerNombre()).append("'; ");
            user.setPrimerNombre(request.getPrimerNombre());
        }
        if (request.getSegundoNombre() != null) {
            cambios.append("segundoNombre: '").append(user.getSegundoNombre())
                    .append("' → '").append(request.getSegundoNombre()).append("'; ");
            user.setSegundoNombre(request.getSegundoNombre());
        }
        if (request.getPrimerApellido() != null) {
            cambios.append("primerApellido: '").append(user.getPrimerApellido())
                    .append("' → '").append(request.getPrimerApellido()).append("'; ");
            user.setPrimerApellido(request.getPrimerApellido());
        }
        if (request.getSegundoApellido() != null) {
            cambios.append("segundoApellido: '").append(user.getSegundoApellido())
                    .append("' → '").append(request.getSegundoApellido()).append("'; ");
            user.setSegundoApellido(request.getSegundoApellido());
        }
        if (request.getCorreoUsuario() != null) {
            cambios.append("correo: '").append(user.getCorreoUsuario())
                    .append("' → '").append(request.getCorreoUsuario()).append("'; ");
            user.setCorreoUsuario(request.getCorreoUsuario());
        }
        if (request.getTelUser() != null) {
            cambios.append("telefono: '").append(user.getTelUser())
                    .append("' → '").append(request.getTelUser()).append("'; ");
            user.setTelUser(request.getTelUser());
        }
        if (request.getGenero() != null) {
            cambios.append("genero: '").append(user.getGenero())
                    .append("' → '").append(request.getGenero()).append("'; ");
            user.setGenero(request.getGenero());
        }
                if (request.getEstadoCuenta() != null) {
                        cambios.append("estadoCuenta: ").append(user.getEstadoCuenta())
                                        .append(" → ").append(request.getEstadoCuenta()).append("; ");
                        user.setEstadoCuenta(request.getEstadoCuenta());
                        user.setActivo(request.getEstadoCuenta() == EstadoCuenta.ACTIVA);
                } else if (request.getActivo() != null) {
            cambios.append("activo: ").append(user.isActivo())
                    .append(" → ").append(request.getActivo()).append("; ");
            user.setActivo(request.getActivo());
                        user.setEstadoCuenta(request.getActivo() ? EstadoCuenta.ACTIVA : EstadoCuenta.DESHABILITADA);
        }

        userRepository.save(user);

        // Registrar en auditoría
        Long adminId = jwtUtil.extractUserId(authHeader.substring(7));
        String adminEmail = jwtUtil.extractCorreoUsuario(authHeader.substring(7));
        auditService.registrar(
                AccionAuditoria.USUARIO_MODIFICADO,
                "User",
                user.getCodigoUsua(),
                adminId,
                adminEmail,
                cambios.toString()
        );

        return ResponseEntity.ok(userMapper.toUserInfoResponse(user));
    }
}
