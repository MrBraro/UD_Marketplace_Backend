package com.udmarketplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response genérico para operaciones que solo retornan un mensaje de confirmación.
 * Usado en endpoints como logout.
 */
@Data
@AllArgsConstructor
public class MessageResponse {

    private String message;
}
