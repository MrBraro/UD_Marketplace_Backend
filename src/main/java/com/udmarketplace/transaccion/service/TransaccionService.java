package com.udmarketplace.transaccion.service;

import com.udmarketplace.transaccion.dto.CrearTransaccionRequest;
import com.udmarketplace.transaccion.dto.FiltroHistorialRequest;
import com.udmarketplace.transaccion.dto.TransaccionDto;

import java.util.List;

/**
 * Contrato del servicio de gestión de transacciones del marketplace UD.
 *
 * <p>Orquesta el flujo completo de compra-venta entre compradores y vendedores:
 * <ol>
 *   <li>Registro de la intención de compra (REQ-05): crea la orden en estado PENDIENTE
 *       asociando comprador, vendedor y producto.</li>
 *   <li>Confirmación por el vendedor  actualiza el estado a CONFIRMADA y
 *       genera automáticamente la orden de entrega con snapshot del producto.</li>
 *   <li>Historial filtrable permite consultar transacciones por comprador,
 *       vendedor, estado y rango de fechas.</li>
 * </ol>
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface TransaccionService {

    /**
     * Registra la intención de compra de un producto por un comprador autenticado .
     * Crea la orden en estado {@code PENDIENTE} asociando comprador, vendedor y producto.
     *
     * @param request        DTO con el identificador del producto a comprar
     * @param codigoComprador identificador del comprador extraído del JWT
     * @return DTO de la transacción creada en estado PENDIENTE
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si el producto no existe o no está disponible
     */
    TransaccionDto registrarIntencioneCompra(CrearTransaccionRequest request, Long codigoComprador);

    /**
     * Confirma una transacción pendiente. Solo el vendedor propietario puede confirmarla .
     * Actualiza el estado a {@code CONFIRMADA} y genera automáticamente la orden de entrega
     * con snapshot del producto.
     *
     * @param idOrden        identificador de la orden a confirmar
     * @param codigoVendedor identificador del vendedor que confirma la transacción
     * @return DTO de la transacción con el detalle de entrega incluido
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si la orden no existe o no pertenece al vendedor
     * @throws com.udmarketplace.auth.exception.OperacionNoPermitidaException si la orden no está en estado PENDIENTE
     */
    TransaccionDto confirmarTransaccion(Long idOrden, Long codigoVendedor);

    /**
     * Retorna el detalle completo de una transacción por su identificador.
     *
     * @param idOrden identificador de la orden
     * @return DTO con todos los datos de la transacción
     * @throws com.udmarketplace.auth.exception.RecursoNoEncontradoException si la orden no existe
     */
    TransaccionDto obtenerTransaccion(Long idOrden);

    /**
     * Consulta el historial de transacciones aplicando filtros opcionales.
     * Todos los parámetros del filtro son opcionales; omitirlos retorna todas las transacciones.
     *
     * @param filtro objeto con los criterios de filtrado (comprador, vendedor, estado, rango de fechas)
     * @return lista de transacciones que cumplen los filtros aplicados
     */
    List<TransaccionDto> consultarHistorial(FiltroHistorialRequest filtro);
}
