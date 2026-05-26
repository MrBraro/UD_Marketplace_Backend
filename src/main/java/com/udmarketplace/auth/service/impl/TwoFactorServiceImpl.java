package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.service.EmailService;
import com.udmarketplace.auth.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Implementación del servicio de autenticación en dos factores (RF11).
 *
 * <p>Alineado al diagrama ER: utiliza correoUsuario para enviar el código 2FA.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorServiceImpl implements TwoFactorService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    /** SecureRandom para generación de códigos criptográficamente seguros. */
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void generateAndSendCode(User user) {
        String code = generateSixDigitCode();

        user.setTwoFactorCode(code);
        userRepository.save(user);

        emailService.sendTwoFactorCode(user.getCorreoUsuario(), code);
        log.debug("Código 2FA generado y enviado para usuario: {}", user.getCorreoUsuario());
    }

    @Override
    public boolean validateCode(User user, String code) {
        return user.getTwoFactorCode() != null
                && user.getTwoFactorCode().equals(code);
    }

    /**
     * Genera un código numérico de 6 dígitos con padding de ceros.
     */
    private String generateSixDigitCode() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }
}
