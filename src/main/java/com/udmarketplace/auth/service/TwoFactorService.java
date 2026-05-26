package com.udmarketplace.auth.service;

import com.udmarketplace.auth.model.User;

/**
 * Contrato del servicio de autenticación en dos factores (RF11).
 *
 * <p>Genera un código numérico de 6 dígitos, lo persiste en el usuario
 * y lo envía al email registrado. También valida el código recibido.
 */
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
