package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.service.FileValidationService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
/**
 * Implementación del servicio de validación de archivos del sistema UD Marketplace.
 *
 * <p>Aplica reglas técnicas al archivo recibido durante el registro de usuarios menores:
 * <ul>
 *   <li>El archivo debe existir y no estar vacío</li>
 *   <li>El tipo MIME debe corresponder a PDF</li>
 *   <li>La extensión debe ser {@code .pdf}</li>
 *   <li>El tamaño no puede exceder el máximo configurado</li>
 * </ul>
 *
 * @version 1.0
 * @since 2026-05-28
 */
@Service
public class FileValidationServiceImpl implements FileValidationService {

    /** Tamaño máximo permitido para el PDF de autorización, en bytes. */
    @Value("${app.files.max-pdf-size-bytes:5242880}")
    private long maxPdfSizeBytes;

    /** Tamaño máximo permitido para imágenes de productos, en bytes. */
    @Value("${app.files.max-image-size-bytes:5242880}")
    private long maxImageSizeBytes;

    /** MIME types válidos para imágenes de productos. */
    private static final Set<String> VALID_IMAGE_MIME_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/webp")
    );

    /** Extensiones válidas para imágenes de productos. */
    private static final Set<String> VALID_IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList(".jpg", ".jpeg", ".png", ".webp")
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El PDF de autorización es obligatorio para usuarios menores de edad");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        boolean mimeValido = "application/pdf".equalsIgnoreCase(contentType);
        boolean extensionValida = originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf");

        if (!mimeValido || !extensionValida) {
            throw new IllegalArgumentException("El archivo adjunto debe estar en formato PDF");
        }

        if (file.getSize() > maxPdfSizeBytes) {
            throw new IllegalArgumentException("El PDF excede el tamaño máximo permitido");
        }
    }
        /**
     * {@inheritDoc}
     */
    @Override
    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La imagen del producto no puede estar vacía");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        // Validar MIME type
        boolean mimeValido = contentType != null &&
                VALID_IMAGE_MIME_TYPES.contains(contentType.toLowerCase());

        if (!mimeValido) {
            throw new IllegalArgumentException(
                    "Tipo de imagen no soportado. Formatos permitidos: JPG, PNG, WebP");
        }

        // Validar extensión
        boolean extensionValida = false;
        if (originalFilename != null) {
            String filenameLC = originalFilename.toLowerCase();
            extensionValida = VALID_IMAGE_EXTENSIONS.stream()
                    .anyMatch(filenameLC::endsWith);
        }

        if (!extensionValida) {
            throw new IllegalArgumentException(
                    "Extensión de archivo no válida. Formatos permitidos: .jpg, .png, .webp");
        }

        // Validar tamaño
        if (file.getSize() > maxImageSizeBytes) {
            throw new IllegalArgumentException("La imagen excede el tamaño máximo permitido");
        }
    }
}