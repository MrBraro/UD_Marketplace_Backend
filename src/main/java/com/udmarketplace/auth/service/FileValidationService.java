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
 * @version 1.1
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

    /**
     * Valida que la imagen sea de un formato soportado, con tipo MIME válido,
     * extensión correcta y tamaño dentro de los límites permitidos (RNF08).
     *
     * @param file archivo de imagen recibido desde una petición multipart/form-data
     * @throws IllegalArgumentException si el archivo no es una imagen válida,
     *                                  tiene un MIME type no soportado,
     *                                  extensión inválida o supera el tamaño permitido
     */
    void validateImage(MultipartFile file);
}