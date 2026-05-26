package com.udmarketplace.auth.service;

import com.udmarketplace.auth.dto.LoginRequest;
import com.udmarketplace.auth.dto.LoginResponse;
import com.udmarketplace.auth.dto.LoginStepResponse;
import com.udmarketplace.auth.dto.TwoFactorRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;

/**
 * Contrato del servicio principal de autenticación.
 *
 * <p>Orquesta el flujo completo de autenticación en dos pasos alineado con el diagrama ER.
 */
public interface AuthService {

    /**
     * Paso 1 del login: valida credenciales y dispara envío de código 2FA (RF08).
     *
     * @param request contiene correoUsuario y passwordUsua
     * @return respuesta indicando que se requiere código 2FA
     */
    LoginStepResponse login(LoginRequest request);

    /**
     * Paso 2 del login: valida código 2FA y emite JWT (RF11).
     *
     * @param request contiene correoUsuario y el código recibido por email
     * @return JWT de sesión + datos del usuario
     */
    LoginResponse verifyTwoFactor(TwoFactorRequest request);

    /**
     * Invalida el token activo de sesión (RF13, RF25).
     *
     * @param token JWT a invalidar (extraído del header Authorization)
     */
    void logout(String token);

    /**
     * Retorna la información del usuario actualmente autenticado.
     *
     * @param correoUsuario correoUsuario extraído del JWT por Spring Security
     * @return información del usuario sin datos sensibles
     */
    UserInfoResponse getCurrentUser(String correoUsuario);
}
