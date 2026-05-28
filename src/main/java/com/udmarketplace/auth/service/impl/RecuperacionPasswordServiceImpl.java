package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.exception.InvalidTokenException;
import com.udmarketplace.auth.model.TokenRecuperacion;
import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.repository.TokenRecuperacionRepository;
import com.udmarketplace.auth.repository.UserRepository;
import com.udmarketplace.auth.service.PythonEmailClientService;
import com.udmarketplace.auth.service.RecuperacionPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación del servicio de recuperación de contraseña del sistema UD Marketplace.
 *
 * <p>Flujo de recuperación en dos pasos:
 * <pre>
 * solicitarRecuperacion():
 *   → Busca el usuario silenciosamente (no lanza excepción si no existe)
 *   → Genera token UUID único y lo persiste con expiración configurable
 *   → Delega el envío del correo al backend Python a través de {@link PythonEmailClientService}
 *
 * resetearPassword():
 *   → Valida que el token exista, no esté usado y no haya expirado
 *   → Actualiza la contraseña con bcrypt
 *   → Marca el token como usado para evitar reutilización
 * </pre>
 *
 * <p>El tiempo de vida del token es configurable mediante {@code app.auth.minutos-expiry-token-recuperacion}
 * (por defecto 60 minutos).
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecuperacionPasswordServiceImpl implements RecuperacionPasswordService {

    /** Repositorio de usuarios para buscar y actualizar datos de la cuenta. */
    private final UserRepository userRepository;

    /** Repositorio de tokens de recuperación para persistencia y validación. */
    private final TokenRecuperacionRepository tokenRecuperacionRepo;

    /** Cliente HTTP hacia el backend Python para el envío de correos. */
    private final PythonEmailClientService pythonEmailClient;

    /** Encoder bcrypt para el hash seguro de la nueva contraseña. */
    private final PasswordEncoder passwordEncoder;

    /** Minutos de vida del token de recuperación antes de expirar. */
    @Value("${app.auth.minutos-expiry-token-recuperacion:60}")
    private int minutosExpiry;

    /**
     * {@inheritDoc}
     *
     * <p>Busca el usuario por correo en modo silencioso — si no existe no lanza excepción
     * para no revelar si el correo está registrado (prevención de enumeración de usuarios).
     */
    @Override
    @Transactional
    public void solicitarRecuperacion(String correoUsuario) {
        // Busca el usuario silenciosamente — no revela si el correo existe
        userRepository.findByCorreoUsuario(correoUsuario).ifPresent(user -> {
            String token = UUID.randomUUID().toString();

            tokenRecuperacionRepo.save(TokenRecuperacion.builder()
                    .usuario(user)
                    .token(token)
                    .fechaExpiracion(LocalDateTime.now().plusMinutes(minutosExpiry))
                    .usado(false)
                    .build());

            // Delega el envío del correo al backend Python
            pythonEmailClient.enviarCorreoRecuperacion(
                    correoUsuario,
                    token,
                    user.getPrimerNombre()
            );

            log.info("Token de recuperación generado para: {}", correoUsuario);
        });
    }

    /**
     * {@inheritDoc}
     *
     * <p>Valida la vigencia del token y actualiza la contraseña con hash bcrypt (REQ seguridad).
     */
    @Override
    @Transactional
    public void resetearPassword(String token, String nuevaPassword) {
        TokenRecuperacion tokenRec = tokenRecuperacionRepo.findByTokenAndUsadoFalse(token)
                .orElseThrow(() -> new InvalidTokenException("Token de recuperación inválido o ya utilizado"));

        if (tokenRec.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("El token de recuperación ha expirado");
        }

        User user = tokenRec.getUsuario();
        user.setPasswordUsua(passwordEncoder.encode(nuevaPassword));
        userRepository.save(user);

        tokenRec.setUsado(true);
        tokenRecuperacionRepo.save(tokenRec);

        log.info("Contraseña actualizada para: {}", user.getCorreoUsuario());
    }
}
