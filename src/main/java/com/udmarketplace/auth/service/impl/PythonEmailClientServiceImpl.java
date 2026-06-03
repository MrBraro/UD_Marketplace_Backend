package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.service.PythonEmailClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación HTTP del cliente de correos que delega al mail service Python (C2C-UD).
 *
 * <p>Contratos consumidos (todos requieren header {@code X-API-Key}):
 * <pre>
 *   POST {mail-base-url}/api/v1/emails/auth/send-otp
 *   POST {mail-base-url}/api/v1/emails/auth/register-confirmation
 *   POST {mail-base-url}/api/v1/emails/auth/password-changed
 *   POST {mail-base-url}/api/v1/emails/moderation/account-suspended
 * </pre>
 *
 * <p>Si el servicio no está disponible o la API key no está configurada,
 * cada método registra un warning y retorna sin lanzar excepción (degradación elegante).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PythonEmailClientServiceImpl implements PythonEmailClientService {

    private final RestTemplate restTemplate;

    @Value("${app.python.mail-base-url:http://localhost:8002}")
    private String mailBaseUrl;

    @Value("${app.python.mail-api-key:}")
    private String mailApiKey;

    // ─────────────────────────────────────────────────────────────────────
    // Métodos públicos
    // ─────────────────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void enviarCodigo2FA(User user, String otpCode, int expirationMinutes) {
        String url = mailBaseUrl + "/api/v1/emails/auth/send-otp";

        Map<String, Object> payload = new HashMap<>();
        payload.put("codigo_user", user.getCodigoUsua().intValue());
        payload.put("correo_institu", user.getCorreoUsuario());
        payload.put("otp_code", otpCode);
        payload.put("expiration_minutes", expirationMinutes);
        payload.put("primer_nomb", user.getPrimerNombre());

        post(url, payload, "Código 2FA", user.getCorreoUsuario());
    }

    /** {@inheritDoc} */
    @Override
    public void enviarConfirmacionRegistro(User user) {
        String url = mailBaseUrl + "/api/v1/emails/auth/register-confirmation";

        Map<String, Object> payload = new HashMap<>();
        payload.put("codigo_user", user.getCodigoUsua().intValue());
        payload.put("correo_institu", user.getCorreoUsuario());
        payload.put("primer_nomb", user.getPrimerNombre());
        payload.put("segundo_nom", user.getSegundoNombre());
        payload.put("primer_apel", user.getPrimerApellido());

        post(url, payload, "Confirmación de registro", user.getCorreoUsuario());
    }

    /** {@inheritDoc} */
    @Override
    public void notificarPasswordCambiado(User user, String ipOrigen) {
        String url = mailBaseUrl + "/api/v1/emails/auth/password-changed";

        Map<String, Object> payload = new HashMap<>();
        payload.put("codigo_user", user.getCodigoUsua().intValue());
        payload.put("correo_institu", user.getCorreoUsuario());
        payload.put("fecha_cambio", Instant.now().toString());
        payload.put("primer_nomb", user.getPrimerNombre());
        payload.put("ip_origen", ipOrigen);

        post(url, payload, "Notificación de cambio de contraseña", user.getCorreoUsuario());
    }

    /** {@inheritDoc} */
    @Override
    public void notificarCuentaSuspendida(User user, String motivoSuspension, Integer numeroContrato) {
        String url = mailBaseUrl + "/api/v1/emails/moderation/account-suspended";

        Map<String, Object> payload = new HashMap<>();
        payload.put("codigo_user", user.getCodigoUsua().intValue());
        payload.put("correo_institu", user.getCorreoUsuario());
        payload.put("primer_nomb", user.getPrimerNombre());
        payload.put("motivo_suspension", motivoSuspension);
        payload.put("numero_contrato", numeroContrato != null ? numeroContrato : 0);
        payload.put("fecha_suspension", Instant.now().toString());

        post(url, payload, "Notificación de suspensión", user.getCorreoUsuario());
    }

    /** {@inheritDoc} — Stub: el mail service aún no tiene endpoint para recuperación. */
    @Override
    public void enviarCorreoRecuperacion(String destinatario, String token, String nombreUsuario) {
        log.warn("[MAIL] El mail service no tiene endpoint de recuperación de contraseña. " +
                 "Correo no enviado a: {}", destinatario);
        log.debug("[MAIL-STUB] Token de recuperación (solo desarrollo): {}", token);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helper interno
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Ejecuta un POST al mail service con el header X-API-Key y manejo de errores uniforme.
     */
    private void post(String url, Map<String, Object> payload, String operacion, String destinatario) {
        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, buildHeaders());
            restTemplate.postForObject(url, request, Void.class);
            log.info("[MAIL] {} enviado a: {}", operacion, destinatario);
        } catch (RestClientException e) {
            log.warn("[MAIL] Mail service no disponible. {} no enviado a: {}. Causa: {}",
                    operacion, destinatario, e.getMessage());
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (mailApiKey != null && !mailApiKey.isBlank()) {
            headers.set("X-API-Key", mailApiKey);
        }
        return headers;
    }
}
