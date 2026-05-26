package com.udmarketplace.auth.model;

/**
 * Roles disponibles en el sistema.
 *
 * <p>Cada rol restringe el acceso a diferentes conjuntos de endpoints:
 * <ul>
 *   <li>{@link #ADMIN}  — Acceso administrativo completo (/api/admin/**)</li>
 *   <li>{@link #SELLER} — Gestión de productos y ventas propias (/api/seller/**)</li>
 *   <li>{@link #BUYER}  — Visualización y compra de productos (/api/buyer/**)</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    SELLER,
    BUYER
}
