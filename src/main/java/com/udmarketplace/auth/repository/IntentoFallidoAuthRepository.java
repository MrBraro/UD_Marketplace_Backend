package com.udmarketplace.auth.repository;

import com.udmarketplace.auth.model.IntentoFallidoAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * Repositorio JPA para la entidad {@link IntentoFallidoAuth}.
 *
 * <p>Provee las operaciones de auditoría de intentos de autenticación
 * necesarias para los requerimientos.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
public interface IntentoFallidoAuthRepository extends JpaRepository<IntentoFallidoAuth, Long> {

    /**
     * Cuenta los intentos fallidos de un correo en un período de tiempo determinado.
     *
     * <p>Usado  para verificar si se alcanzó el máximo de intentos
     * fallidos dentro de la ventana de 10 minutos configurada.
     *
     * @param correo correo electrónico que se intentó usar en el login
     * @param desde  inicio del período a evaluar (normalmente {@code ahora - 10 min})
     * @return cantidad de intentos fallidos del correo en el período indicado
     */
    @Query("SELECT COUNT(i) FROM IntentoFallidoAuth i " +
           "WHERE i.correoIntentado = :correo AND i.exitoso = false AND i.fechaHora >= :desde")
    long contarIntentosFallidosDesde(@Param("correo") String correo,
                                     @Param("desde") LocalDateTime desde);
}
