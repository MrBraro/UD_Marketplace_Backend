/**
 * Contrato del servicio de autenticación en dos factores del marketplace UD (2FA).
 *
 * <p>Genera un código numérico de 6 dígitos, lo persiste en el usuario
 * y lo envía al email registrado. También valida el código recibido durante
 * el segundo paso del flujo de autenticación. El código expira en 10 minutos
 * tras su generación.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.service;

import com.udmarketplace.auth.model.User;

public interface TwoFactorService {

    /**
     * Genera un código 2FA de 6 dígitos, lo almacena en el usuario
     * y lo envía al email registrado.
     *
     * @param user usuario al que se le enviará el código
     */
    void generateAndSendCode(User user);

    /**
     * Valida que el código recibido coincida con el almacenado en el usuario.
     *
     * @param user usuario que intenta verificar
     * @param code código recibido del cliente
     * @return {@code true} si el código es válido
     */
    boolean validateCode(User user, String code);
}
