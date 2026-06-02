package com.udmarketplace.auth.controller;

import com.udmarketplace.auth.dto.UpdateUserRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.mapper.UserMapper;
import com.udmarketplace.auth.model.AccionAuditoria;
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
        if (request.getActivo() != null) {
            cambios.append("activo: ").append(user.isActivo())
                    .append(" → ").append(request.getActivo()).append("; ");
            user.setActivo(request.getActivo());
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
