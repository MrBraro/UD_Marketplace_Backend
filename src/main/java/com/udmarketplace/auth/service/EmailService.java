/**
 * Contrato del servicio de envío de emails del marketplace UD.
 *
 * <p>Abstrae el mecanismo de envío para permitir múltiples implementaciones:
 * <ul>
 *   <li>{@code EmailServiceImpl} — delega al backend Python de correos en desarrollo</li>
 *   <li>Futura implementación SMTP directa usando {@code JavaMailSender}</li>
 * </ul>
 *
 * <p>Decisión de diseño: el envío efectivo de emails es responsabilidad del
 * backend Python (gestor de correos); este contrato lo invoca a través de
 * {@link PythonEmailClientService}.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.service;

public interface EmailService {

    /**
     * Envía el código 2FA al email del usuario.
     *
     * @param toEmail dirección de email del destinatario
     * @param code    código 2FA de 6 dígitos a enviar
     */
    void sendTwoFactorCode(String toEmail, String code);
}
