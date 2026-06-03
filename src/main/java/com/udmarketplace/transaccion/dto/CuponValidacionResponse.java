package com.udmarketplace.transaccion.dto;

import lombok.Data;

/**
 * Respuesta del coupon service al validar un cupón de descuento.
 * Mapeada desde el JSON que retorna {@code POST /api/v1/coupon/validar}.
 */
@Data
public class CuponValidacionResponse {
    private boolean valido;
    private String codigoCupon;
    private double porcentajeDescuento;
    private String descripcion;
}
