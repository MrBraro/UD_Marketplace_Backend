package com.udmarketplace.auth.service;

import java.math.BigDecimal;

/**
 * Cliente HTTP hacia el coupon service Python (C2C-UD).
 *
 * <p>Contratos consumidos:
 * <ul>
 *   <li>POST /api/v1/coupons/happy-birthday/{email} — crea cupón de cumpleaños para el usuario</li>
 *   <li>POST /api/v1/users/{email}/coupons/{code}/apply-assigned   — aplica cupón asignado</li>
 *   <li>POST /api/v1/users/{email}/coupons/{code}/apply-unassigned — aplica cupón libre</li>
 *   <li>GET  /api/v1/users/{email}/coupons — lista cupones del usuario</li>
 * </ul>
 */
public interface PythonCouponClientService {

    /**
     * Solicita al coupon service que cree el cupón de cumpleaños para el usuario recién registrado.
     * Falla silenciosamente si el servicio no está disponible.
     *
     * @param correoUsuario correo del nuevo usuario
     */
    void crearCuponCumpleanios(String correoUsuario);

    /**
     * Intenta aplicar un cupón (asignado o libre) al precio original de un producto.
     * Primero prueba como cupón asignado y si falla como cupón libre.
     *
     * @param correoComprador correo del comprador que aplica el cupón
     * @param codigoCupon     código del cupón
     * @param precioOriginal  precio original del producto
     * @return precio final con descuento, o {@code null} si el cupón no es válido / servicio no disponible
     */
    BigDecimal aplicarCupon(String correoComprador, String codigoCupon, BigDecimal precioOriginal);

    /**
     * Retorna la lista de cupones del usuario en formato JSON crudo.
     * Falla silenciosamente retornando null si el servicio no está disponible.
     *
     * @param correoUsuario correo del usuario
     * @return respuesta JSON o null
     */
    String obtenerCuponesUsuario(String correoUsuario);
}
