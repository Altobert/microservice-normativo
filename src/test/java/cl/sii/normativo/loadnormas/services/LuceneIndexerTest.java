package cl.sii.normativo.loadnormas.services;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LuceneIndexerTest {

    @TempDir
    Path tempDir;

    private LuceneIndexer luceneIndexer;
    private Directory testDirectory;
    private StandardAnalyzer analyzer;

    @BeforeEach
    void setUp() throws IOException {
        // Crear directorio temporal para pruebas
        Path indexPath = tempDir.resolve("test-index");
        Files.createDirectories(indexPath);
        
        testDirectory = new ByteBuffersDirectory(); // Usar directorio en memoria para pruebas
        analyzer = new StandardAnalyzer();
        
        // Crear instancia del servicio con directorio de prueba
        luceneIndexer = new LuceneIndexer();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (testDirectory != null) {
            testDirectory.close();
        }
    }

    @Test
    void testIndexFile_Success() throws IOException {
        // Arrange
        String fileName = "test-document.pdf";
        String content = "Este es un documento de prueba con contenido importante sobre impuestos.";

        // Act
        luceneIndexer.indexFile(fileName, content, testDirectory);

        // Assert - Verificar que el documento se indexó correctamente
        try (DirectoryReader reader = DirectoryReader.open(testDirectory)) {
            assertEquals(1, reader.numDocs());
            
            Document doc = reader.document(0);
            assertEquals(fileName, doc.get("filename"));
            assertEquals(content, doc.get("content"));
            assertEquals("pdf", doc.get("type"));
            assertEquals("es", doc.get("language"));
        }
    }

    @Test
    void testIndexFile_MultipleDocuments() throws IOException {
        // Arrange
        String fileName1 = "document1.pdf";
        String content1 = "Primer documento sobre IVA y impuestos.";
        String fileName2 = "document2.pdf";
        String content2 = "Segundo documento sobre servicios públicos.";

        // Act
        luceneIndexer.indexFile(fileName1, content1, testDirectory);
        luceneIndexer.indexFile(fileName2, content2, testDirectory);

        // Assert
        try (DirectoryReader reader = DirectoryReader.open(testDirectory)) {
            assertEquals(2, reader.numDocs());
            
            // Verificar primer documento
            Document doc1 = reader.document(0);
            assertEquals(fileName1, doc1.get("filename"));
            assertEquals(content1, doc1.get("content"));
            
            // Verificar segundo documento
            Document doc2 = reader.document(1);
            assertEquals(fileName2, doc2.get("filename"));
            assertEquals(content2, doc2.get("content"));
        }
    }

    @Test
    void testIndexFile_EmptyContent() throws IOException {
        // Arrange
        String fileName = "empty-document.pdf";
        String content = "";

        // Act
        luceneIndexer.indexFile(fileName, content, testDirectory);

        // Assert
        try (DirectoryReader reader = DirectoryReader.open(testDirectory)) {
            assertEquals(1, reader.numDocs());
            
            Document doc = reader.document(0);
            assertEquals(fileName, doc.get("filename"));
            assertEquals("0", doc.get("size")); // Tamaño debe ser 0
        }
    }

    @Test
    void testIndexFile_SpecialCharacters() throws IOException {
        // Arrange
        String fileName = "special-chars.pdf";
        String content = "Documento con caracteres especiales: áéíóú ñü ç @#$%^&*()";

        // Act
        luceneIndexer.indexFile(fileName, content, testDirectory);

        // Assert
        try (DirectoryReader reader = DirectoryReader.open(testDirectory)) {
            assertEquals(1, reader.numDocs());
            
            Document doc = reader.document(0);
            assertEquals(fileName, doc.get("filename"));
            assertEquals(content, doc.get("content"));
        }
    }

    @Test
    void testIndexFile_LargeContent() throws IOException {
        // Arrange
        String fileName = "large-document.pdf";
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("Esta es una línea de contenido repetido. ");
        }
        String content = largeContent.toString();

        // Act
        luceneIndexer.indexFile(fileName, content, testDirectory);

        // Assert
        try (DirectoryReader reader = DirectoryReader.open(testDirectory)) {
            assertEquals(1, reader.numDocs());
            
            Document doc = reader.document(0);
            assertEquals(fileName, doc.get("filename"));
            assertEquals(content, doc.get("content"));
            assertEquals(String.valueOf(content.length()), doc.get("size"));
        }
    }

    @Test
    void testIndexFile_NullFileName() {
        // Arrange
        String fileName = null;
        String content = "Contenido de prueba";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            luceneIndexer.indexFile(fileName, content, testDirectory);
        });
    }

    @Test
    void testIndexFile_NullContent() throws IOException {
        // Arrange
        String fileName = "test.pdf";
        String content = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            luceneIndexer.indexFile(fileName, content, testDirectory);
        });
    }

    @Test
    void testSearchFunctionality() throws IOException, ParseException {
        // Arrange - Indexar documentos de prueba
        luceneIndexer.indexFile("doc1.pdf", "Documento sobre impuestos internos y IVA", testDirectory);
        luceneIndexer.indexFile("doc2.pdf", "Información sobre servicios públicos y contribuyentes", testDirectory);
        luceneIndexer.indexFile("doc3.pdf", "Normativa fiscal y tributaria", testDirectory);

        // Act - Realizar búsqueda
        try (DirectoryReader reader = DirectoryReader.open(testDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", analyzer);
            
            Query query = parser.parse("impuestos");
            TopDocs results = searcher.search(query, 10);
            
            // Assert
            assertEquals(1, results.totalHits.value); // Solo doc1 contiene "impuestos"
            
            // Verificar que los documentos encontrados contienen la palabra clave
            for (ScoreDoc hit : results.scoreDocs) {
                Document doc = searcher.doc(hit.doc);
                assertTrue(doc.get("content").toLowerCase().contains("impuestos"));
            }
        }
    }
}
