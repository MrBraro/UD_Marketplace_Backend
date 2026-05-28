/**
 * Enumeración de roles disponibles en el marketplace UD, mapeados al diagrama ER.
 *
 * <p>Usados en el campo {@code rol_usua} de la entidad {@link User} y en las anotaciones
 * {@code @PreAuthorize} de los controladores para el control de acceso basado en roles (RBAC).
 * Spring Security añade el prefijo {@code ROLE_} automáticamente al comparar con
 * {@code hasRole(...)}.
 *
 * @author
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.model;

public enum Role {

    /** Rol con acceso total: gestión de usuarios, PQRs, categorías, valoraciones. */
    ADMINISTRADOR,

    /** Rol con acceso a gestión de productos y confirmación de transacciones. */
    VENDEDOR,

    /** Rol con acceso a compras, valoraciones y PQRs del marketplace. */
    COMPRADOR
}
