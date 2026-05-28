package com.udmarketplace.auth.service;

/**
 * Contrato para la comunicación HTTP con el backend Python encargado del envío de correos.
 *
 * <p>El backend Python (gestor de correos y geolocalización) expone endpoints REST que
 * este cliente invoca para enviar correos transaccionales del sistema:
 * <ul>
 *   <li>Correos de recuperación de contraseña con token único</li>
 *   <li>Códigos de verificación en dos pasos (2FA)</li>
 * </ul>
 *
 * <p>Cuando el equipo Python defina y despliegue los contratos, la implementación
 * {@link impl.PythonEmailClientServiceImpl} se conecta automáticamente al endpoint real
 * usando la URL configurada en {@code app.python.base-url}.
 *
 * <p>En entornos de desarrollo, si el Python no está disponible, la implementación
 * actúa como stub: registra los datos en los logs para permitir pruebas end-to-end
 * sin depender del servicio externo (degradación elegante).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface PythonEmailClientService {

    /**
     * Solicita al backend Python que envíe un correo de recuperación de contraseña.
     *
     * @param destinatario correo del usuario
     * @param token        token único de recuperación
     * @param nombreUsuario nombre para personalizar el correo
     */
    void enviarCorreoRecuperacion(String destinatario, String token, String nombreUsuario);

    /**
     * Solicita al backend Python que envíe el código 2FA al usuario.
     *
     * @param destinatario correo del usuario
     * @param codigo       código numérico de 6 dígitos
     */
    void enviarCodigo2FA(String destinatario, String codigo);
}
