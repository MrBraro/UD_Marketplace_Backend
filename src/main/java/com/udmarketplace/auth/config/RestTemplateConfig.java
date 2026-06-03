package com.udmarketplace.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración del cliente HTTP {@link RestTemplate} para llamadas al microservicio
 * Python de correos (C2C-UD email service).
 *
 * <p>Establece timeouts de conexión y lectura para evitar que el hilo de Spring
 * quede bloqueado indefinidamente cuando el email service no está disponible.
 */
@Configuration
public class RestTemplateConfig {

    /** Tiempo máximo en ms para establecer la conexión TCP con el servicio externo. */
    private static final int CONNECT_TIMEOUT_MS = 3_000;

    /** Tiempo máximo en ms para recibir datos una vez abierta la conexión. */
    private static final int READ_TIMEOUT_MS = 5_000;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        return new RestTemplate(factory);
    }
}
