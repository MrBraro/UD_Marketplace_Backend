package com.udmarketplace.catalogo.service.impl;

import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.model.Administrador;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.catalogo.dto.CategoriaDto;
import com.udmarketplace.catalogo.dto.CrearCategoriaRequest;
import com.udmarketplace.catalogo.model.Categoria;
import com.udmarketplace.catalogo.repository.CategoriaRepository;
import com.udmarketplace.catalogo.service.CategoriaService;
import lombok.RequiredArgsConstructor;
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
 * @author Daniel Perez
 * @version 1.0
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
     * <p>Asocia la categoría al administrador identificado por {@code codigoAdmin}.
     */
    @Override
    @Transactional
    public CategoriaDto crearCategoria(CrearCategoriaRequest request, Long codigoAdmin) {
        Administrador admin = (Administrador) userRepository.findById(codigoAdmin)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado"));

        Categoria categoria = Categoria.builder()
                .nombreCat(request.getNombreCat())
                .descripcionCat(request.getDescripcionCat())
                .activoCat(true)
                .contadorProductos(0)
                .administrador(admin)
                .build();

        return toDto(categoriaRepository.save(categoria));
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void inactivarCategoria(Long idCategoria, Long codigoAdmin) {
        Categoria categoria = buscarCategoria(idCategoria);
        categoria.setActivoCat(false);
        categoriaRepository.save(categoria);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void incrementarContador(Long idCategoria) {
        Categoria categoria = buscarCategoria(idCategoria);
        categoria.setContadorProductos(categoria.getContadorProductos() + 1);
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
        int nuevoContador = Math.max(0, categoria.getContadorProductos() - 1);
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
     * Convierte una entidad {@link Categoria} a su DTO de respuesta.
     *
     * @param c entidad de categoría
     * @return {@link com.udmarketplace.catalogo.dto.CategoriaDto} con los datos de la categoría
     */
    private CategoriaDto toDto(Categoria c) {
        return CategoriaDto.builder()
                .idCategoria(c.getIdCategoria())
                .nombreCat(c.getNombreCat())
                .descripcionCat(c.getDescripcionCat())
                .activoCat(c.isActivoCat())
                .contadorProductos(c.getContadorProductos())
                .build();
    }
}
