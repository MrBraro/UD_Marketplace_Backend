package com.udmarketplace.auth.service;

import com.udmarketplace.auth.model.User;

/**
 * Cliente HTTP hacia el mail service Python (C2C-UD).
 *
 * <p>Todos los endpoints requieren el header {@code X-API-Key}.
 * Si el servicio no está disponible, cada método falla silenciosamente
 * (degradación elegante) sin interrumpir el flujo de Java.
 *
 * <p>Contratos consumidos del mail service:
 * <ul>
 *   <li>POST /api/v1/emails/auth/send-otp              — código 2FA</li>
 *   <li>POST /api/v1/emails/auth/register-confirmation — bienvenida tras registro</li>
 *   <li>POST /api/v1/emails/auth/password-changed      — alerta de cambio de contraseña</li>
 *   <li>POST /api/v1/emails/moderation/account-suspended — suspensión de cuenta</li>
 * </ul>
 */
public interface PythonEmailClientService {

    /**
     * Envía el código OTP de 6 dígitos para el segundo factor de autenticación.
     * Llama a {@code POST /api/v1/emails/auth/send-otp}.
     *
     * @param user              usuario destinatario
     * @param otpCode           código numérico de 6 dígitos
     * @param expirationMinutes minutos de vigencia del código (máx 10)
     */
    void enviarCodigo2FA(User user, String otpCode, int expirationMinutes);

    /**
     * Envía el correo de confirmación de registro exitoso.
     * Llama a {@code POST /api/v1/emails/auth/register-confirmation}.
     *
     * @param user usuario recién registrado
     */
    void enviarConfirmacionRegistro(User user);

    /**
     * Notifica que la contraseña fue cambiada exitosamente.
     * Llama a {@code POST /api/v1/emails/auth/password-changed}.
     *
     * @param user     usuario cuya contraseña cambió
     * @param ipOrigen IP desde la que se realizó el cambio (puede ser null)
     */
    void notificarPasswordCambiado(User user, String ipOrigen);

    /**
     * Notifica al usuario que su cuenta fue suspendida.
     * Llama a {@code POST /api/v1/emails/moderation/account-suspended}.
     *
     * @param user             usuario suspendido
     * @param motivoSuspension motivo de la suspensión
     * @param numeroContrato   número de contrato del administrador que suspende
     */
    void notificarCuentaSuspendida(User user, String motivoSuspension, Integer numeroContrato);

    /**
     * PENDIENTE: el mail service no tiene endpoint de recuperación de contraseña.
     * Opera como stub hasta que Python agregue el endpoint.
     *
     * @param destinatario  correo del usuario
     * @param token         token UUID de recuperación
     * @param nombreUsuario nombre para personalizar el correo
     */
    void enviarCorreoRecuperacion(String destinatario, String token, String nombreUsuario);
}
