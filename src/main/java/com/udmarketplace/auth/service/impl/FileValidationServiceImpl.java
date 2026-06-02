package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.service.FileValidationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class FileValidationServiceImpl implements FileValidationService {

    private static final long MAX_PDF_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB limit for images (adjust as needed)
    
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/jpg", "image/webp"
    );

    @Override
    public void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo PDF es obligatorio y no puede estar vacío");
        }
        
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("El archivo adjunto debe ser de formato PDF");
        }
        
        if (file.getSize() > MAX_PDF_SIZE) {
            throw new IllegalArgumentException("El tamaño del archivo PDF no puede exceder los 5MB");
        }
    }

    @Override
    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La imagen es obligatoria y no puede estar vacía");
        }
        
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("El formato de la imagen no es válido. Solo se permiten formatos JPEG, PNG, JPG o WEBP");
        }
        
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("El tamaño de la imagen no puede exceder los 5MB");
        }
        
        String filename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        if (filename.contains("..")) {
            throw new IllegalArgumentException("El nombre del archivo de imagen contiene una secuencia de ruta no válida");
        }
    }
}
