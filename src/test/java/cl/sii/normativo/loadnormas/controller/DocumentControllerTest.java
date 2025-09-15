package cl.sii.normativo.loadnormas.controller;

import cl.sii.normativo.loadnormas.services.LuceneIndexer;
import cl.sii.normativo.loadnormas.services.PDFTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private LuceneIndexer luceneIndexer;

    @InjectMocks
    private DocumentController documentController;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // Crear un archivo PDF simulado
        String pdfContent = "Este es un documento de prueba con contenido de ejemplo.";
        mockFile = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                pdfContent.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    void testUploadPDF_Success() throws Exception {
        // Arrange
        String extractedText = "Contenido extraído del PDF";
        
        try (MockedStatic<PDFTextExtractor> mockedExtractor = mockStatic(PDFTextExtractor.class)) {
            mockedExtractor.when(() -> PDFTextExtractor.extractText(any(ByteArrayInputStream.class)))
                    .thenReturn(extractedText);
            
            doNothing().when(luceneIndexer).indexFile(anyString(), anyString());

            // Act
            ResponseEntity<String> response = documentController.uploadPDF(mockFile);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Documento indexado exitosamente.", response.getBody());
            
            verify(luceneIndexer).indexFile("test-document.pdf", extractedText);
        }
    }

    @Test
    void testUploadPDF_ExtractionError() throws Exception {
        // Arrange
        try (MockedStatic<PDFTextExtractor> mockedExtractor = mockStatic(PDFTextExtractor.class)) {
            mockedExtractor.when(() -> PDFTextExtractor.extractText(any(ByteArrayInputStream.class)))
                    .thenThrow(new Exception("Error al extraer texto del PDF"));

            // Act
            ResponseEntity<String> response = documentController.uploadPDF(mockFile);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().contains("Error al indexar el documento"));
            assertTrue(response.getBody().contains("Error al extraer texto del PDF"));
            
            verify(luceneIndexer, never()).indexFile(anyString(), anyString());
        }
    }

    @Test
    void testUploadPDF_IndexingError() throws Exception {
        // Arrange
        String extractedText = "Contenido extraído del PDF";
        
        try (MockedStatic<PDFTextExtractor> mockedExtractor = mockStatic(PDFTextExtractor.class)) {
            mockedExtractor.when(() -> PDFTextExtractor.extractText(any(ByteArrayInputStream.class)))
                    .thenReturn(extractedText);
            
            doThrow(new IOException("Error al indexar")).when(luceneIndexer)
                    .indexFile(anyString(), anyString());

            // Act
            ResponseEntity<String> response = documentController.uploadPDF(mockFile);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().contains("Error al indexar el documento"));
            assertTrue(response.getBody().contains("Error al indexar"));
        }
    }

    @Test
    void testUploadPDF_EmptyFile() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        // Act
        ResponseEntity<String> response = documentController.uploadPDF(emptyFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Error al indexar el documento"));
    }

    @Test
    void testUploadPDF_NullFile() {
        // Arrange
        MockMultipartFile nullFile = new MockMultipartFile(
                "file",
                "null.pdf",
                "application/pdf",
                new byte[0] // Usar array vacío en lugar de null
        );

        // Act
        ResponseEntity<String> response = documentController.uploadPDF(nullFile);

        // Assert - Debe manejar el archivo vacío correctamente
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Error al indexar el documento"));
    }
}
