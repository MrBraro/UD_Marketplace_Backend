package com.udmarketplace.auth.service;

import com.udmarketplace.auth.dto.LoginRequest;
import com.udmarketplace.auth.dto.LoginResponse;
import com.udmarketplace.auth.dto.LoginStepResponse;
import com.udmarketplace.auth.dto.TwoFactorRequest;
import com.udmarketplace.auth.dto.UserInfoResponse;
import com.udmarketplace.auth.dto.UserResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contrato del servicio principal de autenticación del sistema UD Marketplace.
 *
 * <p>Orquesta el flujo completo de autenticación en dos pasos (2FA):
 * <ol>
 *   <li>Validación de credenciales → generación y envío de código 2FA</li>
 *   <li>Validación del código 2FA → emisión del JWT de sesión</li>
 * </ol>
 *
 * <p>También gestiona el cierre de sesión y la consulta de información del usuario activo.
 *
 * @version 1.1
 * @since 2026-05-28
 */
public interface AuthService {

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>La implementación debe calcular si el usuario es menor de edad a partir
     * de la fecha de nacimiento recibida. Si el usuario es menor, debe exigir
     * un archivo PDF de autorización firmado por el representante legal y asociarlo
     * al registro del usuario.
     *
     * <p>La contraseña recibida no debe almacenarse en texto plano; debe transformarse
     * mediante un algoritmo de hash seguro antes de persistirse.
     *
     * @param request DTO con los datos del usuario a registrar
     * @param pdfAutorizacion archivo PDF de autorización, obligatorio solo para usuarios menores de edad
     * @return DTO con los datos básicos del usuario creado
     * @throws IllegalArgumentException si el correo ya existe, si falta el PDF siendo requerido
     *                                  o si los datos del registro incumplen reglas de negocio
     */
    UserResponse register(RegisterRequest request, MultipartFile pdfAutorizacion);

    /**
     * Paso 1 del login: valida credenciales, registra el intento (REQ-02)
     * y, si son correctas, genera y envía el código 2FA.
     *
     * <p>Si el usuario supera el máximo de intentos fallidos se bloquea
     * temporalmente (REQ-03) y se lanza {@code AccountBlockedException}.
     *
     * @param request  DTO con {@code correoUsuario} y {@code passwordUsua}
     * @param ipOrigen dirección IP del cliente para auditoría (REQ-02)
     * @return respuesta indicando que se requiere código 2FA
     * @throws com.udmarketplace.auth.exception.InvalidCredentialsException si las credenciales son incorrectas
     * @throws com.udmarketplace.auth.exception.AccountBlockedException     si la cuenta está bloqueada
     */
    LoginStepResponse login(LoginRequest request, String ipOrigen);

    /**
     * Paso 2 del login: valida el código 2FA y emite el JWT de sesión (REQ-01).
     *
     * <p>El JWT incluye el {@code userId} del usuario para asociar cada
     * token con su propietario (REQ-01).
     *
     * @param request DTO con {@code correoUsuario} y el código 2FA recibido por correo
     * @return JWT de sesión con datos básicos del usuario autenticado
     * @throws com.udmarketplace.auth.exception.TwoFactorException si el código es inválido o expiró
     */
    LoginResponse verifyTwoFactor(TwoFactorRequest request);

    /**
     * Invalida el token activo de sesión añadiéndolo a la blacklist.
     *
     * @param token JWT compacto extraído del header {@code Authorization: Bearer <token>}
     */
    void logout(String token);

    /**
     * Retorna la información del usuario actualmente autenticado.
     *
     * @param correoUsuario correo extraído del JWT por Spring Security (subject del token)
     * @return DTO con datos del perfil del usuario sin información sensible
     * @throws com.udmarketplace.auth.exception.InvalidCredentialsException si el usuario no existe
     */
    UserInfoResponse getCurrentUser(String correoUsuario);
}
