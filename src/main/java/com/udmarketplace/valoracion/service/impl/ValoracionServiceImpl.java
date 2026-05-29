/**
 * Implementación del servicio de valoraciones del marketplace UD.
 *
 * <p>Gestiona el ciclo completo de una valoración:
 * <ol>
 *   <li>Valida que la orden sea de compra confirmada y pertenezca al comprador (REQ).</li>
 *   <li>Si existe una valoración activa previa del mismo comprador y producto, la inactiva
 *       conservando el historial sin sobrescribir (REQ-17).</li>
 *   <li>Crea la nueva valoración registrando la relación comprador-vendedor (REQ-18).</li>
 *   <li>Recalcula y persiste automáticamente la reputación del vendedor (REQ-16).</li>
 * </ol>
 *
 * <p>Las operaciones de escritura están anotadas con {@code @Transactional} (REQ-23).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.valoracion.service.impl;

import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.model.Comprador;
import com.udmarketplace.auth.model.Vendedor;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.catalogo.model.Producto;
import com.udmarketplace.catalogo.repository.ProductoRepository;
import com.udmarketplace.transaccion.model.EstadoOrden;
import com.udmarketplace.transaccion.model.Orden;
import com.udmarketplace.transaccion.repository.OrdenRepository;
import com.udmarketplace.valoracion.dto.CrearValoracionRequest;
import com.udmarketplace.valoracion.dto.ReputacionVendedorDto;
import com.udmarketplace.valoracion.dto.ValoracionDto;
import com.udmarketplace.valoracion.model.ResenaPredefinida;
import com.udmarketplace.valoracion.model.Valoracion;
import com.udmarketplace.valoracion.repository.ResenaPredefinidaRepository;
import com.udmarketplace.valoracion.repository.ValoracionRepository;
import com.udmarketplace.valoracion.service.ValoracionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValoracionServiceImpl implements ValoracionService {

    /** Repositorio principal de valoraciones. */
    private final ValoracionRepository valoracionRepository;

    /** Repositorio del catálogo de reseñas predefinidas. */
    private final ResenaPredefinidaRepository resenaRepo;

    /** Repositorio de usuarios para recuperar comprador y vendedor. */
    private final UserRepository userRepository;

    /** Repositorio de productos para verificar existencia y obtener vendedor. */
    private final ProductoRepository productoRepository;

    /** Repositorio de órdenes para validar la compra confirmada. */
    private final OrdenRepository ordenRepository;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ValoracionDto registrarValoracion(CrearValoracionRequest request, Long codigoComprador) {
        Comprador comprador = (Comprador) userRepository.findById(codigoComprador)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comprador no encontrado"));

        Producto producto = productoRepository.findById(request.getIdPub())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        Orden orden = ordenRepository.findById(request.getIdOrden())
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        // REQ: solo si el usuario compró el producto
        if (!orden.getComprador().getCodigoUsua().equals(codigoComprador) ||
                !orden.getProducto().getIdPub().equals(request.getIdPub()) ||
                !EstadoOrden.CONFIRMADA.name().equals(orden.getEstadoOrden())) {
            throw new OperacionNoPermitidaException("Solo se puede valorar un producto que haya comprado en una transacción confirmada");
        }

        // REQ-17: impedir duplicar valoración activa
        if (valoracionRepository.existsByComprador_CodigoUsuaAndProducto_IdPubAndEstadoValoTrue(
                codigoComprador, request.getIdPub())) {
            // Inactivar la valoración anterior antes de crear una nueva (historial sin sobrescritura)
            valoracionRepository.findByComprador_CodigoUsuaAndProducto_IdPub(codigoComprador, request.getIdPub())
                    .stream().filter(Valoracion::isEstadoValo)
                    .forEach(v -> {
                        v.setEstadoValo(false);
                        valoracionRepository.save(v);
                    });
        }

        ResenaPredefinida resena = null;
        if (request.getIdResena() != null) {
            resena = resenaRepo.findById(request.getIdResena()).orElse(null);
        }

        Valoracion valoracion = Valoracion.builder()
                .comprador(comprador)
                .vendedor(producto.getVendedor())
                .producto(producto)
                .orden(orden)
                .calificacion(request.getCalificacion())
                .resenaPredefinida(resena)
                .valoPredefinida(resena != null ? resena.getTextoResena() : null)
                .fechaValo(LocalDate.now())
                .estadoValo(true)
                .build();

        Valoracion guardada = valoracionRepository.save(valoracion);

        // REQ-16: actualizar calificación del vendedor
        actualizarCalificacionVendedor(producto.getVendedor());

        return toDto(guardada);
    }

    /** {@inheritDoc} */
    @Override
    public Double calcularPromedioProducto(Long idPub) {
        return valoracionRepository.calcularPromedioProducto(idPub);
    }

    /** {@inheritDoc} */
    @Override
    public ReputacionVendedorDto obtenerReputacionVendedor(Long codigoVendedor) {
        Vendedor vendedor = (Vendedor) userRepository.findById(codigoVendedor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vendedor no encontrado"));

        Double promedio = valoracionRepository.calcularReputacionVendedor(codigoVendedor);
        // REQ-19: reseñas positivas (calificación >= 4)
        long positivas = valoracionRepository.contarResenasPositivas(codigoVendedor);
        long total = valoracionRepository.findByVendedor_CodigoUsuaAndEstadoValoTrue(codigoVendedor).size();

        return ReputacionVendedorDto.builder()
                .idVendedor(codigoVendedor)
                .nombreVendedor(vendedor.getPrimerNombre() + " " + vendedor.getPrimerApellido())
                .calificacionPromedio(promedio != null ? Math.round(promedio * 100.0) / 100.0 : 0.0)
                .totalResenasPositivas(positivas)
                .totalValoraciones(total)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    public List<ValoracionDto> listarValoracionesProducto(Long idPub) {
        return valoracionRepository.findByProducto_IdPubAndEstadoValoTrue(idPub)
                .stream().map(this::toDto).toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<ResenaPredefinida> listarResenasPredefinidas() {
        return resenaRepo.findByActivoTrue();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void inactivarValoracion(Long idVal) {
        Valoracion valoracion = valoracionRepository.findById(idVal)
                .orElseThrow(() -> new RecursoNoEncontradoException("Valoración no encontrada: " + idVal));
        valoracion.setEstadoValo(false);
        valoracionRepository.save(valoracion);
        actualizarCalificacionVendedor(valoracion.getVendedor());
    }

    // ------------------------------------------------------------------
    // Métodos privados de soporte
    // ------------------------------------------------------------------

    /**
     * Recalcula y persiste la calificación (reputación) de un vendedor como el promedio
     * redondeado a dos decimales de todas sus valoraciones activas (REQ-16).
     * Si no hay valoraciones activas, establece la calificación en cero.
     *
     * @param vendedor entidad del vendedor cuya reputación se debe actualizar
     */
    private void actualizarCalificacionVendedor(Vendedor vendedor) {
        Double promedio = valoracionRepository.calcularReputacionVendedor(vendedor.getCodigoUsua());
        vendedor.setCalificacion(promedio != null
                ? new java.math.BigDecimal(promedio).setScale(2, java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO);
        userRepository.save(vendedor);
    }

    /**
     * Convierte una entidad {@link Valoracion} a su DTO de respuesta.
     *
     * @param v entidad de valoración a convertir
     * @return DTO {@link ValoracionDto} con los datos de la valoración
     */
    private ValoracionDto toDto(Valoracion v) {
        return ValoracionDto.builder()
                .idVal(v.getIdVal())
                .idProducto(v.getProducto().getIdPub())
                .nombreProducto(v.getProducto().getNombrePub())
                .idVendedor(v.getVendedor().getCodigoUsua())
                .nombreVendedor(v.getVendedor().getPrimerNombre() + " " + v.getVendedor().getPrimerApellido())
                .idComprador(v.getComprador().getCodigoUsua())
                .nombreComprador(v.getComprador().getPrimerNombre() + " " + v.getComprador().getPrimerApellido())
                .calificacion(v.getCalificacion())
                .resenaPredefinida(v.getValoPredefinida())
                .fechaValo(v.getFechaValo())
                .estadoValo(v.isEstadoValo())
                .build();
    }
}
