package cl.sii.normativo.loadnormas.integration;

import cl.sii.normativo.loadnormas.services.LuceneIndexer;
import cl.sii.normativo.loadnormas.services.PDFTextExtractor;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DocumentIntegrationTest {

    private LuceneIndexer luceneIndexer;
    private Directory testDirectory;
    private StandardAnalyzer analyzer;

    @BeforeEach
    void setUp() throws IOException {
        testDirectory = new ByteBuffersDirectory();
        analyzer = new StandardAnalyzer();
        luceneIndexer = new LuceneIndexer();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (testDirectory != null) {
            testDirectory.close();
        }
    }

    @Test
    void testCompleteDocumentWorkflow() throws Exception {
        // Arrange - Simular contenido de PDF
        String fileName = "normativa-fiscal.pdf";
        String pdfContent = """
            NORMA FISCAL N° 1234
            Servicio de Impuestos Internos
            
            Artículo 1: Definiciones
            Para efectos de esta normativa, se entiende por:
            a) IVA: Impuesto al Valor Agregado
            b) Contribuyente: Persona natural o jurídica obligada al pago de impuestos
            
            Artículo 2: Obligaciones
            Los contribuyentes deben cumplir con las siguientes obligaciones:
            1. Declarar mensualmente el IVA
            2. Mantener registros contables actualizados
            3. Presentar documentos de respaldo
            
            Artículo 3: Sanciones
            El incumplimiento de estas obligaciones será sancionado conforme a la ley.
            """;

        // Act - Simular el flujo completo
        // 1. Simular extracción de texto (en lugar de usar PDFTextExtractor con texto plano)
        String extractedText = pdfContent; // Simulamos que ya tenemos el texto extraído

        // 2. Indexar el documento
        luceneIndexer.indexFile(fileName, extractedText, testDirectory);

        // Assert - Verificar que el documento se indexó correctamente
        try (DirectoryReader reader = DirectoryReader.open(testDirectory)) {
            assertEquals(1, reader.numDocs());
            
            Document doc = reader.document(0);
            assertEquals(fileName, doc.get("filename"));
            assertEquals("pdf", doc.get("type"));
            assertEquals("es", doc.get("language"));
            assertTrue(doc.get("content").contains("IVA"));
            assertTrue(doc.get("content").contains("contribuyente"));
        }
    }

    @Test
    void testSearchFunctionality_CompleteWorkflow() throws Exception {
        // Arrange - Indexar múltiples documentos
        String[] fileNames = {
            "normativa-iva.pdf",
            "manual-contribuyente.pdf",
            "guia-sanciones.pdf"
        };
        
        String[] contents = {
            "Normativa sobre IVA y obligaciones tributarias para contribuyentes",
            "Manual del contribuyente con guías de cumplimiento fiscal",
            "Guía de sanciones y multas por incumplimiento tributario"
        };

        // Indexar documentos
        for (int i = 0; i < fileNames.length; i++) {
            luceneIndexer.indexFile(fileNames[i], contents[i], testDirectory);
        }

        // Act - Realizar búsquedas
        try (DirectoryReader reader = DirectoryReader.open(testDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", analyzer);

            // Búsqueda por "IVA"
            Query query1 = parser.parse("IVA");
            TopDocs results1 = searcher.search(query1, 10);
            assertEquals(1, results1.totalHits.value);
            assertEquals("normativa-iva.pdf", searcher.doc(results1.scoreDocs[0].doc).get("filename"));

            // Búsqueda por "contribuyente"
            Query query2 = parser.parse("contribuyente");
            TopDocs results2 = searcher.search(query2, 10);
            assertEquals(1, results2.totalHits.value); // Solo manual-contribuyente.pdf contiene "contribuyente"

            // Búsqueda por "sanciones"
            Query query3 = parser.parse("sanciones");
            TopDocs results3 = searcher.search(query3, 10);
            assertEquals(1, results3.totalHits.value);
            assertEquals("guia-sanciones.pdf", searcher.doc(results3.scoreDocs[0].doc).get("filename"));

            // Búsqueda que no existe
            Query query4 = parser.parse("inexistente");
            TopDocs results4 = searcher.search(query4, 10);
            assertEquals(0, results4.totalHits.value);
        }
    }

    @Test
    void testMultiplePDFProcessing() throws Exception {
        // Arrange - Simular procesamiento de múltiples PDFs
        String[] documents = {
            "Documento sobre normativa fiscal y tributaria en Chile",
            "Guía de cumplimiento para empresas del sector servicios",
            "Manual de procedimientos para declaración de impuestos",
            "Normativa sobre IVA y retenciones tributarias"
        };

        // Act - Procesar cada documento
        for (int i = 0; i < documents.length; i++) {
            String fileName = "documento-" + (i + 1) + ".pdf";
            luceneIndexer.indexFile(fileName, documents[i], testDirectory);
        }

        // Assert - Verificar que todos los documentos se indexaron
        try (DirectoryReader reader = DirectoryReader.open(testDirectory)) {
            assertEquals(4, reader.numDocs());
            
            // Verificar que cada documento tiene los campos correctos
            for (int i = 0; i < 4; i++) {
                Document doc = reader.document(i);
                assertNotNull(doc.get("filename"));
                assertNotNull(doc.get("content"));
                assertNotNull(doc.get("type"));
                assertNotNull(doc.get("language"));
                assertNotNull(doc.get("size"));
                assertNotNull(doc.get("createdDate"));
            }
        }
    }

    @Test
    void testErrorHandling_InvalidPDF() {
        // Arrange - Simular PDF inválido
        byte[] invalidPdfData = "Esto no es un PDF válido".getBytes(StandardCharsets.UTF_8);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            PDFTextExtractor.extractText(new ByteArrayInputStream(invalidPdfData));
        });
    }

    @Test
    void testPerformance_LargeDocument() throws Exception {
        // Arrange - Crear un documento grande
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("Línea ").append(i).append(": Contenido de prueba para documento grande. ");
        }
        
        String fileName = "documento-grande.pdf";
        String content = largeContent.toString();

        // Act - Medir tiempo de indexación
        long startTime = System.currentTimeMillis();
        luceneIndexer.indexFile(fileName, content, testDirectory);
        long endTime = System.currentTimeMillis();

        // Assert - Verificar que se indexó correctamente y en tiempo razonable
        try (DirectoryReader reader = DirectoryReader.open(testDirectory)) {
            assertEquals(1, reader.numDocs());
            
            Document doc = reader.document(0);
            assertEquals(fileName, doc.get("filename"));
            assertEquals(String.valueOf(content.length()), doc.get("size"));
        }

        // Verificar que el tiempo de indexación es razonable (menos de 5 segundos)
        long processingTime = endTime - startTime;
        assertTrue(processingTime < 5000, "El procesamiento tomó demasiado tiempo: " + processingTime + "ms");
    }
}
