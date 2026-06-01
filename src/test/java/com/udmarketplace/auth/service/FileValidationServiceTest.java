/**
 * Pruebas unitarias extras para FileValidationServiceImpl.
 * Verifica que el servicio rechaza archivos nulos o vacíos, MIME types inválidos, extensiones inválidas y archivos que exceden el tamaño máximo, y que acepta archivos válidos.
 *
 * @version 1.0
 * @since 2026-05-28
 */
package com.udmarketplace.auth.service;

import com.udmarketplace.auth.service.impl.FileValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileValidationServiceTest")
class FileValidationServiceTest {

    private static final long FIVE_MB = 5_242_880L;

    @InjectMocks
    private FileValidationServiceImpl fileValidationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileValidationService, "maxPdfSizeBytes", FIVE_MB);
        ReflectionTestUtils.setField(fileValidationService, "maxImageSizeBytes", FIVE_MB);
    }

    @Test
    @DisplayName("Debe rechazar PDF nulo")
    void validatePdf_null_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileValidationService.validatePdf(null)
        );
        assertEquals("El PDF de autorización es obligatorio para usuarios menores de edad", ex.getMessage());
    }

    @Test
    @DisplayName("Debe rechazar PDF vacío")
    void validatePdf_vacio_lanzaExcepcion() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileValidationService.validatePdf(file)
        );
        assertEquals("El PDF de autorización es obligatorio para usuarios menores de edad", ex.getMessage());
    }

    @Test
    @DisplayName("Debe rechazar PDF con MIME inválido")
    void validatePdf_mimeInvalido_lanzaExcepcion() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getOriginalFilename()).thenReturn("permiso.pdf");
        when(file.getSize()).thenReturn(1000L);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileValidationService.validatePdf(file)
        );
        assertEquals("El archivo adjunto debe estar en formato PDF", ex.getMessage());
    }

    @Test
    @DisplayName("Debe rechazar PDF con extensión inválida")
    void validatePdf_extensionInvalida_lanzaExcepcion() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("permiso.txt");
        when(file.getSize()).thenReturn(1000L);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileValidationService.validatePdf(file)
        );
        assertEquals("El archivo adjunto debe estar en formato PDF", ex.getMessage());
    }

    @Test
    @DisplayName("Debe rechazar PDF demasiado grande")
    void validatePdf_muyGrande_lanzaExcepcion() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("permiso.pdf");
        when(file.getSize()).thenReturn(FIVE_MB + 1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileValidationService.validatePdf(file)
        );
        assertEquals("El PDF excede el tamaño máximo permitido", ex.getMessage());
    }

    @Test
    @DisplayName("Debe aceptar PDF válido")
    void validatePdf_valido_noLanzaExcepcion() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("permiso.pdf");
        when(file.getSize()).thenReturn(1000L);

        assertDoesNotThrow(() -> fileValidationService.validatePdf(file));
    }

    @Test
    @DisplayName("Debe rechazar imagen nula")
    void validateImage_null_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileValidationService.validateImage(null)
        );
        assertEquals("La imagen del producto no puede estar vacía", ex.getMessage());
    }

    @Test
    @DisplayName("Debe rechazar imagen vacía")
    void validateImage_vacia_lanzaExcepcion() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file)
        );
        assertEquals("La imagen del producto no puede estar vacía", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/pdf", "image/gif", "image/bmp", "text/plain"})
    @DisplayName("Debe rechazar imágenes con MIME no soportado")
    void validateImage_mimeInvalido_lanzaExcepcion(String mimeType) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(mimeType);
        when(file.getOriginalFilename()).thenReturn("archivo.jpg");
        when(file.getSize()).thenReturn(1000L);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file)
        );
        assertEquals("Tipo de imagen no soportado. Formatos permitidos: JPG, PNG, WebP", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"archivo.txt", "archivo.pdf", "archivo.exe", "archivo.svg"})
    @DisplayName("Debe rechazar imágenes con extensión no permitida")
    void validateImage_extensionInvalida_lanzaExcepcion(String filename) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.getSize()).thenReturn(1000L);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file)
        );
        assertEquals("Extensión de archivo no válida. Formatos permitidos: .jpg, .png, .webp", ex.getMessage());
    }

    @Test
    @DisplayName("Debe rechazar imagen demasiado grande")
    void validateImage_muyGrande_lanzaExcepcion() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("imagen.png");
        when(file.getSize()).thenReturn(FIVE_MB + 1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file)
        );
        assertEquals("La imagen excede el tamaño máximo permitido", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"foto.jpg", "foto.jpeg", "foto.png", "foto.webp"})
    @DisplayName("Debe aceptar imágenes válidas")
    void validateImage_valida_noLanzaExcepcion(String filename) {
        String mimeType = filename.endsWith(".png") ? "image/png"
                : filename.endsWith(".webp") ? "image/webp"
                : "image/jpeg";

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(mimeType);
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.getSize()).thenReturn(1000L);

        assertDoesNotThrow(() -> fileValidationService.validateImage(file));
    }

    @Test
    @DisplayName("Debe aceptar imagen en el límite exacto de tamaño")
    void validateImage_enLimite_noLanzaExcepcion() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("imagen.png");
        when(file.getSize()).thenReturn(FIVE_MB);

        assertDoesNotThrow(() -> fileValidationService.validateImage(file));
    }
}