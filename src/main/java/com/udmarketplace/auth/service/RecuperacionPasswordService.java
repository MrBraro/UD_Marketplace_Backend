package com.udmarketplace.auth.service;

/**
 * Contrato del servicio de recuperación de contraseña del sistema UD Marketplace.
 *
 * <p>Gestiona el flujo de dos pasos para que un usuario recupere el acceso a su cuenta:
 * <ol>
 *   <li>Solicitud de recuperación → genera token único y delega el envío del correo al backend Python.</li>
 *   <li>Reseteo de contraseña → valida el token recibido y actualiza la contraseña con hash bcrypt.</li>
 * </ol>
 *
 * <p>El token de recuperación tiene tiempo de vida configurable (por defecto 60 minutos) y
 * solo puede usarse una vez; una vez consumido queda marcado como {@code usado = true}.
 *
 * @version 1.0
 * @since 2026-05-28
 */
public interface RecuperacionPasswordService {

    /**
     * Genera un token UUID único de recuperación, lo persiste con su fecha de expiración
     * y delega al backend Python el envío del correo con el enlace de recuperación.
     *
     * <p>Este método <b>nunca</b> revela si el correo existe o no en el sistema;
     * siempre retorna sin lanzar excepción para evitar enumeración de usuarios.
     *
     * @param correoUsuario correo electrónico del usuario que solicita la recuperación
     */
    void solicitarRecuperacion(String correoUsuario);

    /**
     * Valida el token de recuperación recibido, actualiza la contraseña del usuario
     * con hash bcrypt y marca el token como usado para evitar reutilización.
     *
     * @param token          token UUID de recuperación enviado al correo del usuario
     * @param nuevaPassword  nueva contraseña en texto plano (mínimo 8 caracteres)
     * @throws com.udmarketplace.auth.exception.InvalidTokenException si el token no existe,
     *         ya fue utilizado o ha expirado
     */
    void resetearPassword(String token, String nuevaPassword);
}
