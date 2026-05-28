package com.udmarketplace.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración del cliente HTTP {@link RestTemplate} para llamadas al backend Python.
 *
 * <p>Registra un bean de {@link RestTemplate} en el contexto de Spring para ser
 * inyectado en los servicios que necesiten comunicarse con el backend Python
 * de gestión de correos y geolocalización.
 *
 * <p>En caso de requerir timeouts personalizados o interceptores de logging,
 * este bean puede extenderse con {@link org.springframework.http.client.SimpleClientHttpRequestFactory}.
 *
 * @author Daniel Perez
 * @version 1.0
 * @since 2026-05-28
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Crea y registra una instancia de {@link RestTemplate} con configuración por defecto.
     *
     * @return instancia de RestTemplate lista para inyección
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
