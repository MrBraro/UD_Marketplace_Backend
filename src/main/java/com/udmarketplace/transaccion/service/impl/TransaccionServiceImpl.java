package com.udmarketplace.transaccion.service.impl;

import com.udmarketplace.auth.exception.OperacionNoPermitidaException;
import com.udmarketplace.auth.exception.RecursoNoEncontradoException;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.model.Vendedor;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.catalogo.model.Producto;
import com.udmarketplace.catalogo.repository.ProductoRepository;
import com.udmarketplace.transaccion.dto.CrearTransaccionRequest;
import com.udmarketplace.transaccion.dto.FiltroHistorialRequest;
import com.udmarketplace.transaccion.dto.OrdenEntregaDto;
import com.udmarketplace.transaccion.dto.TransaccionDto;
import com.udmarketplace.transaccion.model.DetalleOrdenEntrega;
import com.udmarketplace.transaccion.model.EstadoOrden;
import com.udmarketplace.transaccion.model.Orden;
import com.udmarketplace.transaccion.repository.DetalleOrdenEntregaRepository;
import com.udmarketplace.transaccion.repository.OrdenRepository;
import com.udmarketplace.transaccion.service.TransaccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del servicio de transacciones del marketplace UD.
 *
 * <p>Orquesta el flujo de compra-venta: registro de intención, confirmación por el vendedor
 * y consulta del historial filtrable. Todas las operaciones de escritura son transaccionales.
 *
 * <p>Al confirmar una transacción se generan automáticamente:
 * <ul>
 *   <li>Cambio de estado: {@code PENDIENTE → CONFIRMADA} </li>
 *   <li>Orden de entrega con snapshot del producto </li>
 *   <li>Código de confirmación digital único: {@code CONF-{idOrden}-{UUID8}}</li>
 * </ul>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Service
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {

    /** Repositorio de órdenes para operaciones CRUD y consultas de historial. */
    private final OrdenRepository ordenRepository;

    /** Repositorio del detalle de orden de entrega para el snapshot del producto. */
    private final DetalleOrdenEntregaRepository detalleRepo;

    /** Repositorio de usuarios para obtener la entidad del comprador. */
    private final UserRepository userRepository;

    /** Repositorio de productos para obtener el producto y verificar disponibilidad. */
    private final ProductoRepository productoRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Verifica que el producto esté activo y disponible antes de crear la orden.
     * El vendedor se asigna automáticamente del producto.
     */
    @Override
    @Transactional
    public TransaccionDto registrarIntencioneCompra(CrearTransaccionRequest request, Long codigoComprador) {
        User comprador = userRepository.findById(codigoComprador)
        .orElseThrow(() -> new RecursoNoEncontradoException("Comprador no encontrado"));

        if (!comprador.isActivo()) {
            throw new OperacionNoPermitidaException("El comprador no está activo");
        }

        Producto producto = productoRepository.findById(request.getIdPub())
                .filter(Producto::isActivoPub)
                .filter(Producto::isDisponibilidad)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no disponible: " + request.getIdPub()));

        if (producto.getVendedor().getCodigoUsua().equals(comprador.getCodigoUsua())) {
            throw new OperacionNoPermitidaException("No puedes registrar intención de compra sobre tu propio producto");
        }

        // asociar comprador, vendedor y producto
        Orden orden = Orden.builder()
                .comprador(comprador)
                .vendedor(producto.getVendedor())
                .producto(producto)
                .totalCompra(producto.getPrecioPub())
                .estadoOrden(EstadoOrden.PENDIENTE.name())
                .fechaCompr(LocalDate.now())
                .datetimeCompra(LocalDateTime.now())
                .build();

        return toDto(ordenRepository.save(orden));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Solo confirma si la orden está en estado PENDIENTE.
     * Genera automáticamente el detalle de entrega con snapshot del producto.
     */
    @Override
    @Transactional
    public TransaccionDto confirmarTransaccion(Long idOrden, Long codigoVendedor) {
        Orden orden = ordenRepository.findByIdOrdenAndVendedor_CodigoUsua(idOrden, codigoVendedor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transacción no encontrada o no es de este vendedor"));

        if (!EstadoOrden.PENDIENTE.name().equals(orden.getEstadoOrden())) {
            throw new OperacionNoPermitidaException("Solo se pueden confirmar transacciones en estado PENDIENTE");
        }

        //  actualizar estado
        orden.setEstadoOrden(EstadoOrden.CONFIRMADA.name());
        ordenRepository.save(orden);

        // generar orden de entrega automáticamente al confirmar
        // incluir snapshot del producto con sus detalles al momento de la compra
        generarOrdenEntrega(orden);

        return toDto(orden);
    }

    /** {@inheritDoc} */
    @Override
    public TransaccionDto obtenerTransaccion(Long idOrden) {
        Orden orden = ordenRepository.findById(idOrden)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transacción no encontrada: " + idOrden));
        return toDto(orden);
    }

    /** {@inheritDoc} */
    @Override
    public List<TransaccionDto> consultarHistorial(FiltroHistorialRequest filtro) {
        return ordenRepository.buscarHistorial(
                filtro.getCodigoComprador(),
                filtro.getCodigoVendedor(),
                filtro.getEstado(),
                filtro.getDesde(),
                filtro.getHasta()
        ).stream().map(this::toDto).toList();
    }

 
    /**
     * Genera el {@link DetalleOrdenEntrega} con snapshot inmutable del producto
     * al momento de la confirmación de la compra.
     *
     * @param orden orden confirmada para la que se genera el detalle de entrega
     */
    private void generarOrdenEntrega(Orden orden) {
        Producto p = orden.getProducto();
        DetalleOrdenEntrega detalle = DetalleOrdenEntrega.builder()
                .orden(orden)
                .nombreProducto(p.getNombrePub())
                .descripcionProd(p.getDescripcionPub())
                .precioUnitario(p.getPrecioPub())
                .imagenProducto(p.getImagenPub())
                .fechaGeneracion(LocalDateTime.now())
                .confirmacionDigital(generarConfirmacion(orden))
                .build();
        detalleRepo.save(detalle);
        orden.setDetalleEntrega(detalle);
    }

    /**
     * Genera el código de confirmación digital único para la transacción.
     * Formato: {@code CONF-{idOrden}-{8 caracteres UUID en mayúscula}}.
     *
     * @param orden orden confirmada
     * @return código único de confirmación
     */
    private String generarConfirmacion(Orden orden) {
        return "CONF-" + orden.getIdOrden() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Convierte una entidad {@link Orden} a su DTO de respuesta.
     * Incluye el detalle de entrega si ya fue generado (solo en órdenes CONFIRMADAS).
     *
     * @param o entidad de la orden
     * @return DTO completo de la transacción
     */
    private TransaccionDto toDto(Orden o) {
        TransaccionDto dto = TransaccionDto.builder()
                .idOrden(o.getIdOrden())
                .idComprador(o.getComprador().getCodigoUsua())
                .nombreComprador(o.getComprador().getPrimerNombre() + " " + o.getComprador().getPrimerApellido())
                .idVendedor(o.getVendedor() != null ? o.getVendedor().getCodigoUsua() : null)
                .nombreVendedor(o.getVendedor() != null
                        ? o.getVendedor().getPrimerNombre() + " " + o.getVendedor().getPrimerApellido() : null)
                .idProducto(o.getProducto() != null ? o.getProducto().getIdPub() : null)
                .nombreProducto(o.getProducto() != null ? o.getProducto().getNombrePub() : null)
                .totalCompra(o.getTotalCompra())
                .estadoOrden(o.getEstadoOrden())
                .datetimeCompra(o.getDatetimeCompra())
                .build();

        if (o.getDetalleEntrega() != null) {
            DetalleOrdenEntrega d = o.getDetalleEntrega();
            dto.setDetalleEntrega(OrdenEntregaDto.builder()
                    .idDetalle(d.getIdDetalle())
                    .nombreProducto(d.getNombreProducto())
                    .descripcionProd(d.getDescripcionProd())
                    .precioUnitario(d.getPrecioUnitario())
                    .fechaGeneracion(d.getFechaGeneracion())
                    .confirmacionDigital(d.getConfirmacionDigital())
                    .build());
        }
        return dto;
    }
}
