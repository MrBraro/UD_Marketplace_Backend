package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.service.FileValidationService;
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
}