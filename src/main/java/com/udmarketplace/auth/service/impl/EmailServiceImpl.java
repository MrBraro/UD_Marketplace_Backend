package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.service.EmailService;
import com.udmarketplace.auth.service.PythonEmailClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de correo del sistema UD Marketplace.
 *
 * <p>Actúa como fachada que delega todas las operaciones de envío de correo al
 * {@link PythonEmailService}, que a su vez realiza las llamadas HTTP al
 * backend Python de gestión de correos.
 *
 * <p>Esta capa de indirección facilita cambiar el mecanismo de envío en el futuro
 * sin afectar a los consumidores del {@link com.udmarketplace.auth.service.EmailService}.
 *
 * @version 1.1
 * @since 2026-05-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    /** Cliente que realiza las llamadas HTTP al backend Python de correos. */
    private final PythonEmailClientService pythonEmailClient;

    /**
     * Delega el envío del código 2FA al backend Python a través del cliente HTTP.
     *
     * @param toEmail correo electrónico del destinatario
     * @param code    código numérico de 6 dígitos generado para la verificación
     */
    @Override
    public void sendTwoFactorCode(String toEmail, String code) {
        pythonEmailClient.enviarCodigo2FA(toEmail, code);
    }
}
