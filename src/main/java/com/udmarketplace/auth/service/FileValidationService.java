/**
 * Contrato del servicio de validación de archivos adjuntos del sistema UD Marketplace.
 *
 * <p>Centraliza las reglas técnicas aplicadas a documentos cargados por el usuario,
 * especialmente para el PDF de autorización requerido en registros de menores de edad.
 *
 * <p>Las validaciones mínimas incluyen:
 * <ul>
 *   <li>Existencia del archivo</li>
 *   <li>Formato PDF</li>
 *   <li>Tamaño máximo permitido</li>
 * </ul>
 *
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileValidationService {

    /**
     * Valida que el archivo adjunto exista, sea PDF y no exceda el tamaño máximo permitido.
     *
     * @param file archivo recibido desde una petición multipart/form-data
     * @throws IllegalArgumentException si el archivo es nulo, está vacío, no es PDF
     *                                  o supera el tamaño permitido
     */
    void validatePdf(MultipartFile file);
}