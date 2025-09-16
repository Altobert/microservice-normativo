package cl.sii.normativo.loadnormas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

/**
 * Tests unitarios para EmbeddingService.
 */
class EmbeddingServiceTest {

    @Autowired
    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService();
    }

    @Test
    @DisplayName("Debería generar embedding para texto válido")
    void testGenerateEmbeddingValidText() {
        String text = "compra de productos en el exterior";
        Map<String, Double> embedding = embeddingService.generateEmbedding(text);
        
        assertNotNull(embedding);
        assertTrue(embedding.size() > 0);
        assertTrue(embedding.containsKey("keyword_impuesto"));
        assertTrue(embedding.containsKey("keyword_renta"));
        assertTrue(embedding.containsKey("length"));
        assertTrue(embedding.containsKey("word_count"));
    }

    @Test
    @DisplayName("Debería manejar texto nulo")
    void testGenerateEmbeddingNullText() {
        Map<String, Double> embedding = embeddingService.generateEmbedding(null);
        
        assertNotNull(embedding);
        assertEquals(0, embedding.size());
    }

    @Test
    @DisplayName("Debería manejar texto vacío")
    void testGenerateEmbeddingEmptyText() {
        Map<String, Double> embedding = embeddingService.generateEmbedding("");
        
        assertNotNull(embedding);
        assertEquals(0, embedding.size());
    }

    @Test
    @DisplayName("Debería manejar texto con solo espacios")
    void testGenerateEmbeddingWhitespaceText() {
        Map<String, Double> embedding = embeddingService.generateEmbedding("   ");
        
        assertNotNull(embedding);
        assertEquals(0, embedding.size());
    }

    @Test
    @DisplayName("Debería generar embeddings consistentes para el mismo texto")
    void testGenerateEmbeddingConsistency() {
        String text = "IVA impuesto al valor agregado";
        
        Map<String, Double> embedding1 = embeddingService.generateEmbedding(text);
        Map<String, Double> embedding2 = embeddingService.generateEmbedding(text);
        
        assertEquals(embedding1, embedding2);
    }

    @Test
    @DisplayName("Debería generar embeddings diferentes para textos diferentes")
    void testGenerateEmbeddingDifferentTexts() {
        String text1 = "impuestos sobre la renta";
        String text2 = "IVA impuesto al valor agregado";
        
        Map<String, Double> embedding1 = embeddingService.generateEmbedding(text1);
        Map<String, Double> embedding2 = embeddingService.generateEmbedding(text2);
        
        assertNotEquals(embedding1, embedding2);
    }

    @Test
    @DisplayName("Debería calcular similitud coseno correctamente")
    void testCalculateCosineSimilarity() {
        Map<String, Double> embedding1 = Map.of("impuesto", 1.0, "renta", 0.0);
        Map<String, Double> embedding2 = Map.of("impuesto", 1.0, "renta", 0.0);
        
        double similarity = embeddingService.calculateCosineSimilarity(embedding1, embedding2);
        
        assertEquals(1.0, similarity, 0.001);
    }

    @Test
    @DisplayName("Debería calcular similitud coseno para vectores ortogonales")
    void testCalculateCosineSimilarityOrthogonal() {
        Map<String, Double> embedding1 = Map.of("impuesto", 1.0, "renta", 0.0);
        Map<String, Double> embedding2 = Map.of("impuesto", 0.0, "renta", 1.0);
        
        double similarity = embeddingService.calculateCosineSimilarity(embedding1, embedding2);
        
        assertEquals(0.0, similarity, 0.001);
    }

    @Test
    @DisplayName("Debería manejar vectores de diferentes dimensiones")
    void testCalculateCosineSimilarityDifferentDimensions() {
        Map<String, Double> embedding1 = Map.of("impuesto", 1.0);
        Map<String, Double> embedding2 = Map.of("impuesto", 1.0, "renta", 0.0);
        
        // Con Map, diferentes dimensiones no deberían lanzar excepción
        double similarity = embeddingService.calculateCosineSimilarity(embedding1, embedding2);
        assertTrue(similarity >= 0.0 && similarity <= 1.0);
    }

    @Test
    @DisplayName("Debería manejar vectores cero")
    void testCalculateCosineSimilarityZeroVectors() {
        Map<String, Double> embedding1 = Map.of("impuesto", 0.0, "renta", 0.0);
        Map<String, Double> embedding2 = Map.of("impuesto", 1.0, "renta", 0.0);
        
        double similarity = embeddingService.calculateCosineSimilarity(embedding1, embedding2);
        
        assertEquals(0.0, similarity, 0.001);
    }

    @Test
    @DisplayName("Debería manejar cache de embeddings")
    void testEmbeddingCache() {
        String text = "test cache";
        
        // Primera llamada
        Map<String, Double> embedding1 = embeddingService.generateEmbedding(text);
        
        // Segunda llamada (debería usar cache)
        Map<String, Double> embedding2 = embeddingService.generateEmbedding(text);
        
        assertEquals(embedding1, embedding2);
        assertTrue(embeddingService.getCacheSize() > 0);
    }

    @Test
    @DisplayName("Debería limpiar cache correctamente")
    void testClearCache() {
        String text = "test cache clear";
        embeddingService.generateEmbedding(text);
        
        assertTrue(embeddingService.getCacheSize() > 0);
        
        embeddingService.clearCache();
        
        assertEquals(0, embeddingService.getCacheSize());
    }

    @Test
    @DisplayName("Debería verificar disponibilidad del servicio")
    void testIsAvailable() {
        boolean available = embeddingService.isAvailable();
        
        // Con la implementación simplificada, debería ser true
        assertTrue(available);
    }

    @Test
    @DisplayName("Debería generar embeddings para texto largo")
    void testGenerateEmbeddingLongText() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("impuestos sobre la renta ");
        }
        
        Map<String, Double> embedding = embeddingService.generateEmbedding(longText.toString());
        
        assertNotNull(embedding);
        assertTrue(embedding.size() > 0);
        assertTrue(embedding.containsKey("keyword_impuesto"));
        assertTrue(embedding.containsKey("keyword_renta"));
        assertTrue(embedding.containsKey("length"));
        assertTrue(embedding.containsKey("word_count"));
    }

    @Test
    @DisplayName("Debería generar embeddings para texto con caracteres especiales")
    void testGenerateEmbeddingSpecialCharacters() {
        String text = "impuestos@#$%^&*()_+{}|:<>?[]\\;'\",./ sobre la renta";
        
        Map<String, Double> embedding = embeddingService.generateEmbedding(text);
        
        assertNotNull(embedding);
        assertTrue(embedding.size() > 0);
        assertTrue(embedding.containsKey("keyword_impuesto"));
        assertTrue(embedding.containsKey("keyword_renta"));
        assertTrue(embedding.containsKey("length"));
        assertTrue(embedding.containsKey("word_count"));
    }
}
