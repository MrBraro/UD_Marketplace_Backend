package com.udmarketplace.auth.service;

import com.udmarketplace.auth.dto.LoginRequest;
import com.udmarketplace.auth.dto.LoginResponse;
import com.udmarketplace.auth.dto.LoginStepResponse;
import com.udmarketplace.auth.dto.TwoFactorRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;

public interface AuthService {

    /**
     * Paso 1 del login: valida credenciales, registra el intento y envía código 2FA.
     */
    LoginStepResponse login(LoginRequest request, String ipOrigen);

    /**
     * Paso 2 del login: valida el código 2FA y emite el JWT de sesión.
     */
    LoginResponse verifyTwoFactor(TwoFactorRequest request);

    /** Invalida el token activo añadiéndolo a la blacklist. */
    void logout(String token);

    /** Retorna el perfil del usuario autenticado. */
    UserInfoResponse getCurrentUser(String correoUsuario);
}
