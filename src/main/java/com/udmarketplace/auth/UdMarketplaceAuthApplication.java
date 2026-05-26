package com.udmarketplace.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the UD Marketplace Authentication Backend.
 *
 * <p>This application exposes a REST API responsible exclusively for:
 * <ul>
 *   <li>Credential validation (RF08)</li>
 *   <li>Two-factor authentication and JWT emission (RF11)</li>
 *   <li>Token invalidation / logout (RF13, RF25)</li>
 *   <li>Role-based authorization (RF24)</li>
 * </ul>
 */
@SpringBootApplication
public class UdMarketplaceAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(UdMarketplaceAuthApplication.class, args);
    }
}
