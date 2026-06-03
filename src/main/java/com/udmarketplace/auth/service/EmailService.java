package com.udmarketplace.auth.service;

import com.udmarketplace.auth.model.User;

public interface EmailService {

    /**
     * Envía el código 2FA al usuario.
     * El servicio Python requiere datos adicionales del usuario (id, nombre).
     *
     * @param user              usuario destinatario
     * @param code              código numérico de 6 dígitos
     * @param expirationMinutes minutos de vigencia del código
     */
    void sendTwoFactorCode(User user, String code, int expirationMinutes);
}
