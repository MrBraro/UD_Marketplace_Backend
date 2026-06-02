/**
 * Clase de entrada principal del backend del marketplace UD.
 *
 * <p>Arranca la aplicación Spring Boot y habilita el escaneo de componentes
 * en el paquete raíz {@code com.udmarketplace}, abarcando todos los módulos:
 * auth, catálogo, transaccion, pqr y valoracion.
 *
 * @author 
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.udmarketplace")
@EnableJpaRepositories(basePackages = "com.udmarketplace")
@EntityScan(basePackages = "com.udmarketplace")
public class UdMarketplaceAuthApplication {

    /**
     * Punto de entrada de la aplicación.
     *
     * @param args argumentos de línea de comando (no requeridos)
     */
    public static void main(String[] args) {
        SpringApplication.run(UdMarketplaceAuthApplication.class, args);
    }
}
