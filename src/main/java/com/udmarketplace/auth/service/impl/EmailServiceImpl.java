package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación mock del servicio de email para entorno de desarrollo.
 *
 * <p>En lugar de enviar un email real, registra el código en el log de la aplicación.
 * Esto permite probar el flujo completo de 2FA sin configurar un servidor SMTP.
 *
 * <p><strong>TODO:</strong> Reemplazar esta implementación por una que use
 * {@code JavaMailSender} cuando se configure el servidor de email en producción.
 * Agregar la dependencia {@code spring-boot-starter-mail} al pom.xml y
 * configurar {@code spring.mail.*} en {@code application.properties}.
 *
 * <p>Al usar la interfaz {@link EmailService}, el cambio de implementación
 * no requiere modificar ningún otro componente.
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendTwoFactorCode(String toEmail, String code) {
        // En desarrollo: el código se imprime en los logs del servidor
        // En producción: reemplazar con llamada a JavaMailSender
        log.info("============================================================");
        log.info("[EMAIL MOCK] Para: {}", toEmail);
        log.info("[EMAIL MOCK] Asunto: Tu código de verificación - UD Marketplace");
        log.info("[EMAIL MOCK] Código 2FA: {}", code);
        log.info("[EMAIL MOCK] Este código expira con el siguiente login.");
        log.info("============================================================");
    }
}
