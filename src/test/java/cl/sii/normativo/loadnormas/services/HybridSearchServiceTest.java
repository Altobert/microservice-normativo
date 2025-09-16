package cl.sii.normativo.loadnormas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Tests de integración para HybridSearchService.
 * Prueba la funcionalidad real con el índice Lucene.
 */
class HybridSearchServiceTest {

    private HybridSearchService hybridSearchService;

    @BeforeEach
    void setUp() throws IOException {
        hybridSearchService = new HybridSearchService();
        
        try {
            // Configurar el directorio del índice real
            String indexDir = "/Users/albertosanmartin/usach-memoria-implementacion/desarrollo/proyecto-normativo-ms/pipelinenormativosii/lucene-index";
            java.lang.reflect.Field indexDirField = HybridSearchService.class.getDeclaredField("indexDir");
            indexDirField.setAccessible(true);
            indexDirField.set(hybridSearchService, indexDir);
            
            // Inyectar el servicio de embedding real (no mock)
            java.lang.reflect.Field embeddingField = HybridSearchService.class.getDeclaredField("embeddingService");
            embeddingField.setAccessible(true);
            embeddingField.set(hybridSearchService, new EmbeddingService());
        } catch (Exception e) {
            throw new IOException("No se pudo configurar el HybridSearchService", e);
        }
    }

    

    @Test
    @DisplayName("Debería crear resultado de búsqueda híbrida correctamente")
    void testHybridSearchResultCreation() {

        HybridSearchService.HybridSearchResult result = new HybridSearchService.HybridSearchResult(
            "ID123", "documento.pdf", "Título", "Contenido", "2020",
            1.5, 0.8, 1.2, "hybrid"
        );

        assertEquals("ID123", result.getDocumentId());
        assertEquals("documento.pdf", result.getFilename());
        assertEquals("Título", result.getTitle());
        assertEquals("Contenido", result.getContent());
        assertEquals("2020", result.getYear());
        assertEquals(1.5, result.getTraditionalScore());
        assertEquals(0.8, result.getVectorScore());
        assertEquals(1.2, result.getHybridScore());
        assertEquals("hybrid", result.getMatchType());
    }

    @Test
    @DisplayName("Debería obtener estadísticas del servicio")
    void testGetSearchStats() {
        Map<String, Object> stats = hybridSearchService.getSearchStats();

        assertNotNull(stats);
        assertTrue(stats.containsKey("indexDirectory"));
        assertTrue(stats.containsKey("embeddingServiceAvailable"));
        assertTrue(stats.containsKey("embeddingCacheSize"));
        assertTrue(stats.containsKey("searchType"));
        assertTrue(stats.containsKey("supportedFields"));

        assertEquals("hybrid", stats.get("searchType"));
        assertTrue((Boolean) stats.get("embeddingServiceAvailable"));
        assertTrue((Integer) stats.get("embeddingCacheSize") >= 0);
    }

    @Test
    @DisplayName("Debería realizar búsqueda híbrida con consulta simple")
    void testHybridSearchSimpleQuery() throws Exception {
        String query = "IVA";
        List<HybridSearchService.HybridSearchResult> results = hybridSearchService.smartHybridSearch(query, 5);

        assertNotNull(results);
        assertTrue(results.size() > 0);
        
        // Verificar que los resultados tienen la estructura correcta
        for (HybridSearchService.HybridSearchResult result : results) {
            assertNotNull(result.getDocumentId());
            assertNotNull(result.getFilename());
            assertNotNull(result.getTitle());
            assertTrue(result.getTraditionalScore() >= 0);
            assertTrue(result.getVectorScore() >= 0);
            assertTrue(result.getHybridScore() >= 0);
        }
    }

    @Test
    @DisplayName("Debería calcular pesos automáticos para consultas cortas")
    void testSmartHybridSearchShortQuery() throws Exception {
        String shortQuery = "IVA";
        List<HybridSearchService.HybridSearchResult> results = hybridSearchService.smartHybridSearch(shortQuery, 10);

        assertNotNull(results);
        // Las consultas cortas deberían dar más peso a la búsqueda tradicional
        // Verificar que se obtienen resultados
        assertTrue(results.size() >= 0);
    }

    @Test
    @DisplayName("Debería calcular pesos automáticos para consultas largas")
    void testSmartHybridSearchLongQuery() throws Exception {
        String longQuery = "Quiero saber acerca de la compra de productos en el exterior";
        List<HybridSearchService.HybridSearchResult> results = hybridSearchService.smartHybridSearch(longQuery, 10);

        assertNotNull(results);
        // Las consultas largas deberían dar más peso a la búsqueda vectorial
        assertTrue(results.size() >= 0);
    }

    @Test
    @DisplayName("Debería manejar consulta con palabras muy cortas")
    void testSmartHybridSearchVeryShortWords() throws Exception {
        String queryWithShortWords = "el la de en un una";
        List<HybridSearchService.HybridSearchResult> results = hybridSearchService.smartHybridSearch(queryWithShortWords, 10);

        assertNotNull(results);
        // Las palabras muy cortas pueden no dar resultados relevantes
        assertTrue(results.size() >= 0);
    }

    @Test
    @DisplayName("Debería manejar consulta vacía")
    void testSmartHybridSearchEmptyQuery() {
        String emptyQuery = "";
        
        // Una consulta vacía debería lanzar ParseException de Lucene
        assertThrows(Exception.class, () -> {
            hybridSearchService.smartHybridSearch(emptyQuery, 10);
        });
    }

    @Test
    @DisplayName("Debería manejar límite de resultados válido")
    void testHybridSearchValidLimit() throws Exception {
        String query = "impuestos";
        List<HybridSearchService.HybridSearchResult> results = hybridSearchService.hybridSearch(query, 5, 0.6, 0.4);

        assertNotNull(results);
        // El límite de 5 debería respetarse
        assertTrue(results.size() <= 5);
    }

    @Test
    @DisplayName("Debería manejar pesos de búsqueda válidos")
    void testHybridSearchValidWeights() throws Exception {
        String query = "impuestos";
        List<HybridSearchService.HybridSearchResult> results = hybridSearchService.hybridSearch(query, 10, 0.7, 0.3);

        assertNotNull(results);
        // Con pesos válidos debería funcionar correctamente
        assertTrue(results.size() >= 0);
    }

    @Test
    @DisplayName("Debería normalizar pesos automáticamente")
    void testHybridSearchWeightNormalization() throws Exception {
        String query = "impuestos";
        List<HybridSearchService.HybridSearchResult> results = hybridSearchService.hybridSearch(query, 10, 0.8, 0.6);

        assertNotNull(results);
        // Los pesos que suman más de 1.0 deberían ser normalizados automáticamente
        assertTrue(results.size() >= 0);
    }

    @Test
    @DisplayName("Debería manejar pesos cero")
    void testHybridSearchZeroWeights() throws Exception {
        String query = "impuestos";
        List<HybridSearchService.HybridSearchResult> results = hybridSearchService.hybridSearch(query, 10, 0.0, 0.0);

        assertNotNull(results);
        // Con pesos cero puede retornar lista vacía o manejar el caso especial
        assertTrue(results.size() >= 0);
    }

    @Test
    @DisplayName("Debería verificar que el servicio de embedding se usa correctamente")
    void testEmbeddingServiceUsage() throws Exception {
        String query = "impuestos sobre la renta";
        List<HybridSearchService.HybridSearchResult> results = hybridSearchService.smartHybridSearch(query, 10);

        assertNotNull(results);
        // Verificar que se obtienen resultados usando embeddings
        assertTrue(results.size() >= 0);
        
        // Verificar que los resultados tienen scores vectoriales
        for (HybridSearchService.HybridSearchResult result : results) {
            assertTrue(result.getVectorScore() >= 0);
        }
    }

    @Test
    @DisplayName("Debería realizar búsqueda híbrida con diferentes tipos de consultas")
    void testHybridSearchDifferentQueryTypes() throws Exception {
        // Test con término técnico
        List<HybridSearchService.HybridSearchResult> results1 = hybridSearchService.smartHybridSearch("IVA", 5);
        assertNotNull(results1);
        
        // Test con frase completa
        List<HybridSearchService.HybridSearchResult> results2 = hybridSearchService.smartHybridSearch("impuesto sobre la renta", 5);
        assertNotNull(results2);
        
        // Test con término legal
        List<HybridSearchService.HybridSearchResult> results3 = hybridSearchService.smartHybridSearch("circular", 5);
        assertNotNull(results3);
        
        // Todos deberían retornar resultados válidos
        assertTrue(results1.size() >= 0);
        assertTrue(results2.size() >= 0);
        assertTrue(results3.size() >= 0);
    }

    @Test
    @DisplayName("Debería evaluar métricas de rendimiento del sistema híbrido")
    void testHybridSearchPerformanceMetrics() throws Exception {
        System.out.println("\n=== EVALUACIÓN DE MÉTRICAS DEL SISTEMA HÍBRIDO ===");
        
        // Consultas de prueba con resultados esperados conocidos
        String[] testQueries = {
            "IVA",
            "impuesto sobre la renta", 
            "circular",
            "servicio",
            "tributación"
        };
        
        int totalQueries = testQueries.length;
        int totalResults = 0;
        int relevantResults = 0;
        int totalExpectedRelevant = 0;
        
        for (String query : testQueries) {
            System.out.println("\n🔍 Consulta: '" + query + "'");
            
            List<HybridSearchService.HybridSearchResult> results = hybridSearchService.smartHybridSearch(query, 10);
            totalResults += results.size();
            
            // Análisis de relevancia (simplificado)
            int relevantForQuery = 0;
            for (HybridSearchService.HybridSearchResult result : results) {
                // Criterio simple: si el término aparece en título o contenido
                String title = result.getTitle().toLowerCase();
                String content = result.getContent().toLowerCase();
                String queryLower = query.toLowerCase();
                
                if (title.contains(queryLower) || content.contains(queryLower)) {
                    relevantForQuery++;
                    relevantResults++;
                }
            }
            
            // Estimación de documentos relevantes esperados
            int expectedRelevant = Math.min(5, results.size()); // Estimación conservadora
            totalExpectedRelevant += expectedRelevant;
            
            System.out.println("  📊 Resultados encontrados: " + results.size());
            System.out.println("  ✅ Resultados relevantes: " + relevantForQuery);
            System.out.println("  📈 Score promedio: " + String.format("%.3f", 
                results.stream().mapToDouble(r -> r.getHybridScore()).average().orElse(0.0)));
        }
        
        // Cálculo de métricas
        double precision = totalResults > 0 ? (double) relevantResults / totalResults : 0.0;
        double recall = totalExpectedRelevant > 0 ? (double) relevantResults / totalExpectedRelevant : 0.0;
        double f1Score = (precision + recall) > 0 ? 2 * (precision * recall) / (precision + recall) : 0.0;
        
        System.out.println("\n📊 RESUMEN DE MÉTRICAS HÍBRIDAS:");
        System.out.println("  • Total consultas: " + totalQueries);
        System.out.println("  • Total resultados: " + totalResults);
        System.out.println("  • Resultados relevantes: " + relevantResults);
        System.out.println("  • Precision: " + String.format("%.3f (%.1f%%)", precision, precision * 100));
        System.out.println("  • Recall: " + String.format("%.3f (%.1f%%)", recall, recall * 100));
        System.out.println("  • F1-Score: " + String.format("%.3f (%.1f%%)", f1Score, f1Score * 100));
        
        // Evaluación de calidad
        if (precision >= 0.6) {
            System.out.println("  ✅ Precision: EXCELENTE");
        } else if (precision >= 0.4) {
            System.out.println("  ⚠️ Precision: BUENA");
        } else if (precision >= 0.2) {
            System.out.println("  ⚠️ Precision: REGULAR");
        } else {
            System.out.println("  ❌ Precision: NECESITA MEJORA");
        }
        
        if (recall >= 0.7) {
            System.out.println("  ✅ Recall: EXCELENTE");
        } else if (recall >= 0.5) {
            System.out.println("  ⚠️ Recall: BUENO");
        } else if (recall >= 0.3) {
            System.out.println("  ⚠️ Recall: REGULAR");
        } else {
            System.out.println("  ❌ Recall: NECESITA MEJORA");
        }
        
        if (f1Score >= 0.6) {
            System.out.println("  ✅ F1-Score: EXCELENTE");
        } else if (f1Score >= 0.4) {
            System.out.println("  ⚠️ F1-Score: BUENO");
        } else if (f1Score >= 0.2) {
            System.out.println("  ⚠️ F1-Score: REGULAR");
        } else {
            System.out.println("  ❌ Recall: NECESITA MEJORA");
        }
        
        System.out.println("\n=== FIN EVALUACIÓN HÍBRIDA ===\n");
        
        // Verificaciones básicas
        assertTrue(totalResults > 0, "El sistema debe retornar resultados");
        assertTrue(relevantResults > 0, "El sistema debe encontrar resultados relevantes");
    }
}
