package com.udmarketplace.catalogo.service.impl;

import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.model.Administrador;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.catalogo.dto.CategoriaDto;
import com.udmarketplace.catalogo.dto.CrearCategoriaRequest;
import com.udmarketplace.catalogo.model.Categoria;
import com.udmarketplace.catalogo.repository.CategoriaRepository;
import com.udmarketplace.catalogo.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de gestión de categorías del catálogo UD Marketplace.
 *
 * <p>Gestiona el ciclo de vida de las categorías incluyendo la actualización automática
 * del contador de productos activos al registrar o inactivar publicaciones (REQ-04).
 *
* <p>La eliminación de categorías es siempre lógica ({@code activoCat = false});
 * los registros nunca se eliminan físicamente para preservar la integridad referencial.
 *
 * <p>Restricciones funcionales:
 * <ul>
 *   <li>Solo administradores pueden crear categorías (RF28)</li>
 *   <li>Solo administradores pueden inactivar categorías (RF30)</li>
 * </ul>
 * 
 * @author Daniel Perez
 * @version 1.1
 * @since 2026-05-28
 */
@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    /** Repositorio de categorías para operaciones CRUD. */
    private final CategoriaRepository categoriaRepository;

    /** Repositorio de usuarios para validar que el administrador existe. */
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Asocia la categoría al administrador identificado por {@code codigoAdmin}
     * y evita duplicados entre categorías activas.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")

    public CategoriaDto crearCategoria(CrearCategoriaRequest request, Long codigoAdmin) {
        Administrador admin = obtenerAdministrador(codigoAdmin);

        String nombreNormalizado = request.getNombreCat().trim();

        if (categoriaRepository.existsByNombreCatIgnoreCaseAndActivoCatTrue(nombreNormalizado)) {
            throw new OperacionNoPermitidaException("Ya existe una categoría activa con ese nombre");
        }

        Categoria categoria = Categoria.builder()
                .nombreCat(nombreNormalizado)
                .descripcionCat(request.getDescripcionCat() != null ? request.getDescripcionCat().trim() : null)
                .activoCat(true)
                .contadorProductos(0)
                .administrador(admin)
                .build();

        return toDto(categoriaRepository.save(categoria));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)

    public List<CategoriaDto> listarCategoriasActivas() {
        return categoriaRepository.findByActivoCatTrue().stream()
                .map(this::toDto)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public CategoriaDto obtenerCategoria(Long idCategoria) {
        return toDto(buscarCategoria(idCategoria));
    }

    /** {@inheritDoc}
     *
     * <p>Realiza inactivación lógica de la categoría.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CategoriaDto inactivarCategoria(Long idCategoria, Long codigoAdmin) {
        obtenerAdministrador(codigoAdmin);

        Categoria categoria = buscarCategoria(idCategoria);

        if (!categoria.isActivoCat()) {
            throw new OperacionNoPermitidaException("La categoría ya se encuentra inactiva");
        }

        categoria.setActivoCat(false);
        return toDto(categoriaRepository.save(categoria));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void incrementarContador(Long idCategoria) {
        Categoria categoria = buscarCategoria(idCategoria);
        int actual = categoria.getContadorProductos() != null ? categoria.getContadorProductos() : 0;
        categoria.setContadorProductos(actual + 1);
        categoriaRepository.save(categoria);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Usa {@link Math#max} para garantizar que el contador no sea negativo.
     */
    @Override
    @Transactional
    public void decrementarContador(Long idCategoria) {
        Categoria categoria = buscarCategoria(idCategoria);
        int actual = categoria.getContadorProductos() != null ? categoria.getContadorProductos() : 0;
        int nuevoContador = Math.max(0, actual - 1);
        categoria.setContadorProductos(nuevoContador);
        categoriaRepository.save(categoria);
    }

    /**
     * Busca una categoría por ID o lanza excepción si no existe.
     *
     * @param id identificador de la categoría
     * @return entidad {@link Categoria} encontrada
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si no existe
     */
    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada: " + id));
    }

    /**
     * Valida que el usuario exista y corresponda realmente a un administrador.
     *
     * @param codigoAdmin identificador del usuario
     * @return administrador válido
     */
    private Administrador obtenerAdministrador(Long codigoAdmin) {
        User user = userRepository.findById(codigoAdmin)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado"));

        if (!(user instanceof Administrador)) {
            throw new OperacionNoPermitidaException("El usuario indicado no corresponde a un administrador");
        }

        return (Administrador) user;
    }
    /**
     * Convierte una entidad {@link Categoria} a su DTO de respuesta.
     *
     * @param c entidad de categoría
     * @return {@link CategoriaDto} con los datos de la categoría
     */
    private CategoriaDto toDto(Categoria c) {
        Long codigoAdmin = null;
        String nombreAdmin = null;

        if (c.getAdministrador() != null) {
            codigoAdmin = c.getAdministrador().getCodigoUsua();
            nombreAdmin = c.getAdministrador().getPrimerNombre() + " " + c.getAdministrador().getPrimerApellido();
        }
        
        return CategoriaDto.builder()
                .idCategoria(c.getIdCategoria())
                .nombreCat(c.getNombreCat())
                .descripcionCat(c.getDescripcionCat())
                .activoCat(c.isActivoCat())
                .contadorProductos(c.getContadorProductos())
                .codigoAdmin(codigoAdmin)
                .nombreAdmin(nombreAdmin)
                .build();
    }
}
