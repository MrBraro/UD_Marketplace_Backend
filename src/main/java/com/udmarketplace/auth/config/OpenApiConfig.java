package com.udmarketplace.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI 3.0 / Swagger UI para la documentación de la API.
 *
 * <p>Registra el esquema de seguridad Bearer JWT para que los endpoints
 * protegidos puedan probarse directamente desde Swagger UI en
 * {@code /swagger-ui.html}.
 *
 * @version 1.0
 * @since 2026-06-01
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura la especificación OpenAPI con:
     * <ul>
     *   <li>Título, versión y descripción del proyecto</li>
     *   <li>Esquema de seguridad Bearer JWT global</li>
     * </ul>
     *
     * @return configuración OpenAPI personalizada
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("UD Marketplace — API REST")
                        .version("1.0.0")
                        .description(
                                "API REST del Marketplace Universitario de la Universidad Distrital. "
                              + "Incluye autenticación 2FA, catálogo, transacciones, valoraciones, "
                              + "PQR, administración de usuarios y auditoría.")
                        .contact(new Contact()
                                .name("Equipo UD Marketplace")
                                .email("soporte@udmarketplace.com")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtenido tras completar la verificación 2FA")));
    }
}
