package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.service.EmailService;
import com.udmarketplace.auth.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Implementación del servicio de autenticación en dos factores (2FA).
 *
 * <p>Genera un código numérico de 6 dígitos usando {@link SecureRandom} para
 * garantizar aleatoriedad criptográfica. El código se persiste en el usuario
 * junto con su tiempo de expiración (máximo 10 minutos — RNF).
 *
 * <p>El envío del código se delega al {@link EmailService}, que a su vez
 * lo enruta al backend Python de gestión de correos.
 *
 * @version 1.0
 * @since 2026-05-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorServiceImpl implements TwoFactorService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    /** Generador criptográficamente seguro para los códigos 2FA. */
    private final SecureRandom secureRandom = new SecureRandom();

    /** Minutos de vida del código 2FA antes de expirar. */
    @Value("${app.auth.minutos-expiry-2fa:10}")
    private int minutosExpiryTwoFactor;

    /**
     * Genera un código 2FA de 6 dígitos, lo persiste en el usuario con su
     * expiración y solicita el envío por correo.
     *
     * @param user usuario destino del código 2FA
     */
    @Override
    public void generateAndSendCode(User user) {
        String code = generateSixDigitCode();
        user.setTwoFactorCode(code);
        // El código expira en el tiempo configurado (máximo 10 min por RNF)
        user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(minutosExpiryTwoFactor));
        userRepository.save(user);
        emailService.sendTwoFactorCode(user.getCorreoUsuario(), code);
        log.debug("Código 2FA generado para usuario: {}", user.getCorreoUsuario());
    }

    /**
     * Valida que el código ingresado coincida con el almacenado y que no haya expirado.
     *
     * @param user usuario cuyo código 2FA se va a validar
     * @param code código ingresado por el usuario
     * @return {@code true} si el código es correcto y está vigente; {@code false} en caso contrario
     */
    @Override
    public boolean validateCode(User user, String code) {
        if (user.getTwoFactorCode() == null || code == null) {
            return false;
        }

        if (user.getTwoFactorExpiry() != null
                && user.getTwoFactorExpiry().isBefore(LocalDateTime.now())) {

            log.debug("Código 2FA expirado para usuario: {}", user.getCorreoUsuario());

            //invalidar el código 2FA cuando ya expiró.
            user.setTwoFactorCode(null);
            user.setTwoFactorExpiry(null);
            userRepository.save(user);

            return false;
        }

        return user.getTwoFactorCode().equals(code);
    }

    /**
     * Genera un código numérico de 6 dígitos con padding de ceros a la izquierda.
     *
     * @return cadena de 6 dígitos (ej. "042731")
     */
    private String generateSixDigitCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}
