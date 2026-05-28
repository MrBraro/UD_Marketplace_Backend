package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.service.PythonEmailClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación HTTP del cliente de correos que delega al backend Python.
 *
 * <p>Realiza llamadas REST hacia los endpoints del backend Python encargado del
 * envío de correos transaccionales. Los contratos esperados son:
 * <pre>
 *   POST {app.python.base-url}/api/email/recuperacion
 *   Body: { "destinatario": "...", "token": "...", "nombre": "..." }
 *
 *   POST {app.python.base-url}/api/email/2fa
 *   Body: { "destinatario": "...", "codigo": "..." }
 * </pre>
 *
 * <p>Estrategia de resiliencia: si el servidor Python no está disponible
 * ({@link org.springframework.web.client.RestClientException}), se registra
 * la información en los logs como {@code [PYTHON-EMAIL-STUB]} y se retorna
 * sin lanzar excepción. Esto permite ejecutar el backend Java en desarrollo
 * sin depender del servicio externo (degradación elegante).
 *
 * <p>La URL base del Python se configura mediante {@code app.python.base-url}
 * (por defecto {@code http://localhost:5000}).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PythonEmailClientServiceImpl implements PythonEmailClientService {

    /** Cliente HTTP para llamadas REST al backend Python. */
    private final RestTemplate restTemplate;

    /** URL base del backend Python, configurable por entorno. */
    @Value("${app.python.base-url:http://localhost:5000}")
    private String pythonBaseUrl;

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code POST {pythonBaseUrl}/api/email/recuperacion} con los datos
     * del usuario para que Python genere y envíe el correo de recuperación.
     *
     * @param destinatario  correo electrónico del usuario destinatario
     * @param token         token UUID único de recuperación
     * @param nombreUsuario nombre del usuario para personalizar el correo
     */
    @Override
    public void enviarCorreoRecuperacion(String destinatario, String token, String nombreUsuario) {
        String url = pythonBaseUrl + "/api/email/recuperacion";

        Map<String, String> payload = new HashMap<>();
        payload.put("destinatario", destinatario);
        payload.put("token", token);
        payload.put("nombre", nombreUsuario);

        try {
            restTemplate.postForObject(url, payload, Void.class);
            log.info("[PYTHON-EMAIL] Correo de recuperación enviado a: {}", destinatario);
        } catch (RestClientException e) {
            // Stub de simulación: loguea el token para pruebas cuando Python no está disponible
            log.warn("[PYTHON-EMAIL-STUB] Python no disponible. Token de recuperación para {}: {}", destinatario, token);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code POST {pythonBaseUrl}/api/email/2fa} para que Python
     * envíe el código de verificación de 6 dígitos al usuario.
     *
     * @param destinatario correo electrónico del usuario destinatario
     * @param codigo       código numérico de 6 dígitos generado con {@link java.security.SecureRandom}
     */
    @Override
    public void enviarCodigo2FA(String destinatario, String codigo) {
        String url = pythonBaseUrl + "/api/email/2fa";

        Map<String, String> payload = new HashMap<>();
        payload.put("destinatario", destinatario);
        payload.put("codigo", codigo);

        try {
            restTemplate.postForObject(url, payload, Void.class);
            log.info("[PYTHON-EMAIL] Código 2FA enviado a: {}", destinatario);
        } catch (RestClientException e) {
            log.warn("[PYTHON-EMAIL-STUB] Python no disponible. Código 2FA para {}: {}", destinatario, codigo);
        }
    }
}
