package cl.sii.normativo.loadnormas.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PDFTextExtractorTest {

    @TempDir
    Path tempDir;

    @Test
    void testExtractText_Success() throws Exception {
        // Arrange - Crear un PDF simple en memoria
        String expectedText = "Este es un documento de prueba con contenido de ejemplo.";
        byte[] pdfBytes = createSimplePDF(expectedText);
        
        try (InputStream inputStream = new ByteArrayInputStream(pdfBytes)) {
            // Act
            String extractedText = PDFTextExtractor.extractText(inputStream);
            
            // Assert
            assertNotNull(extractedText);
            assertTrue(extractedText.contains("Este es un documento de prueba"));
            assertTrue(extractedText.contains("contenido de ejemplo"));
        }
    }

    @Test
    void testExtractText_EmptyPDF() throws Exception {
        // Arrange - Crear un PDF vacío
        byte[] emptyPdfBytes = createEmptyPDF();
        
        try (InputStream inputStream = new ByteArrayInputStream(emptyPdfBytes)) {
            // Act
            String extractedText = PDFTextExtractor.extractText(inputStream);
            
            // Assert
            assertNotNull(extractedText);
            assertTrue(extractedText.trim().isEmpty() || extractedText.length() < 10);
        }
    }

    @Test
    void testExtractText_MultiplePages() throws Exception {
        // Arrange - Crear un PDF con múltiples páginas
        String page1Text = "Primera página del documento.";
        String page2Text = "Segunda página con más contenido.";
        byte[] multiPagePdfBytes = createMultiPagePDF(page1Text, page2Text);
        
        try (InputStream inputStream = new ByteArrayInputStream(multiPagePdfBytes)) {
            // Act
            String extractedText = PDFTextExtractor.extractText(inputStream);
            
            // Assert
            assertNotNull(extractedText);
            assertTrue(extractedText.contains(page1Text));
            assertTrue(extractedText.contains(page2Text));
        }
    }

    @Test
    void testExtractText_SpecialCharacters() throws Exception {
        // Arrange - Crear un PDF con caracteres especiales
        String specialText = "Documento con caracteres especiales: áéíóú ñü ç @#$%^&*()";
        byte[] specialPdfBytes = createSimplePDF(specialText);
        
        try (InputStream inputStream = new ByteArrayInputStream(specialPdfBytes)) {
            // Act
            String extractedText = PDFTextExtractor.extractText(inputStream);
            
            // Assert
            assertNotNull(extractedText);
            assertTrue(extractedText.contains("caracteres especiales"));
        }
    }

    @Test
    void testExtractText_NullInputStream() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            PDFTextExtractor.extractText(null);
        });
    }

    @Test
    void testExtractText_InvalidPDF() throws Exception {
        // Arrange - Crear bytes que no son un PDF válido
        byte[] invalidPdfBytes = "Esto no es un PDF válido".getBytes();
        
        try (InputStream inputStream = new ByteArrayInputStream(invalidPdfBytes)) {
            // Act & Assert
            assertThrows(Exception.class, () -> {
                PDFTextExtractor.extractText(inputStream);
            });
        }
    }

    @Test
    void testExtractText_LargeDocument() throws Exception {
        // Arrange - Crear un PDF con mucho contenido
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            largeContent.append("Esta es una línea de contenido repetido número ").append(i).append(". ");
        }
        byte[] largePdfBytes = createSimplePDF(largeContent.toString());
        
        try (InputStream inputStream = new ByteArrayInputStream(largePdfBytes)) {
            // Act
            String extractedText = PDFTextExtractor.extractText(inputStream);
            
            // Assert
            assertNotNull(extractedText);
            assertTrue(extractedText.length() > 1000); // Debe tener mucho contenido
            assertTrue(extractedText.contains("línea de contenido repetido"));
        }
    }

    // Métodos auxiliares para crear PDFs de prueba

    private byte[] createSimplePDF(String text) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(25, 750);
                contentStream.showText(text);
                contentStream.endText();
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createEmptyPDF() throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createMultiPagePDF(String page1Text, String page2Text) throws Exception {
        try (PDDocument document = new PDDocument()) {
            // Primera página
            PDPage page1 = new PDPage();
            document.addPage(page1);
            
            try (PDPageContentStream contentStream1 = new PDPageContentStream(document, page1)) {
                contentStream1.beginText();
                contentStream1.setFont(PDType1Font.HELVETICA, 12);
                contentStream1.newLineAtOffset(25, 750);
                contentStream1.showText(page1Text);
                contentStream1.endText();
            }
            
            // Segunda página
            PDPage page2 = new PDPage();
            document.addPage(page2);
            
            try (PDPageContentStream contentStream2 = new PDPageContentStream(document, page2)) {
                contentStream2.beginText();
                contentStream2.setFont(PDType1Font.HELVETICA, 12);
                contentStream2.newLineAtOffset(25, 750);
                contentStream2.showText(page2Text);
                contentStream2.endText();
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}
