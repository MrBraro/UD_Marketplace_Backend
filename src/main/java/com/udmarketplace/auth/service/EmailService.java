package com.udmarketplace.auth.service;

/**
 * Contrato del servicio de envío de emails.
 *
 * <p>Abstrae el mecanismo de envío para permitir múltiples implementaciones:
 * <ul>
 *   <li>{@code EmailServiceImpl} — mock/log para desarrollo</li>
 *   <li>Futura implementación SMTP usando {@code JavaMailSender}</li>
 * </ul>
 *
 * <p>Decisión de diseño: no se usa directamente JavaMailSender en esta etapa
 * para evitar dependencia de configuración SMTP que no es parte del alcance actual.
 */
public interface EmailService {

    /**
     * Envía el código 2FA al email del usuario.
     *
     * @param toEmail dirección de email del destinatario
     * @param code    código 2FA de 6 dígitos a enviar
     */
    void sendTwoFactorCode(String toEmail, String code);
}
