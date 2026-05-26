package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response del endpoint GET /api/auth/me.
 *
 * <p>Retorna la información básica del usuario autenticado actualmente.
 * No expone datos sensibles (password, código 2FA).
 */
@Data
@AllArgsConstructor
public class UserInfoResponse {

    private String username;
    private String email;
    private String role;
}
