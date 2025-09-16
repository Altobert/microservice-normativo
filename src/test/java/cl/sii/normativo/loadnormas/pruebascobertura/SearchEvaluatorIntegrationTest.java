package cl.sii.normativo.loadnormas.pruebascobertura;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import java.io.IOException;
import java.util.List;

/**
 * Pruebas de integración para el evaluador de búsqueda.
 * 
 * Estas pruebas evalúan el rendimiento real del sistema de búsqueda
 * utilizando la clase EvaluationMetrics para medir la calidad de los resultados.
 */
class SearchEvaluatorIntegrationTest {

    private SearchEvaluator searchEvaluator;

    @BeforeEach
    void setUp() throws IOException {
        searchEvaluator = new SearchEvaluator();
        // Configurar el directorio del índice para las pruebas
        java.lang.reflect.Field indexDirField;
        try {
            indexDirField = SearchEvaluator.class.getDeclaredField("indexDir");
            indexDirField.setAccessible(true);
            indexDirField.set(searchEvaluator, "/Users/albertosanmartin/usach-memoria-implementacion/desarrollo/proyecto-normativo-ms/pipelinenormativosii/lucene-index");
        } catch (Exception e) {
            throw new IOException("No se pudo configurar el directorio del índice", e);
        }
        searchEvaluator.initialize();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (searchEvaluator != null) {
            searchEvaluator.close();
        }
    }

    @Test
    @DisplayName("Debería evaluar correctamente una consulta individual")
    void shouldEvaluateSingleQueryCorrectly() throws Exception {
        // Given
        Set<String> relevantIds = new HashSet<>();
        relevantIds.add("ID1302");
        relevantIds.add("ID1627");
        relevantIds.add("ID1122");
        relevantIds.add("ID041");
        
        SearchEvaluator.TestQuery testQuery = new SearchEvaluator.TestQuery(
            "IVA",
            relevantIds,
            "Búsqueda de documentos sobre IVA"
        );

        // When
        EvaluationMetrics metrics = searchEvaluator.evaluateSearch(testQuery, 10);

        // Then
        assertNotNull(metrics);
        assertTrue(metrics.getTruePositives() >= 0);
        assertTrue(metrics.getFalsePositives() >= 0);
        assertTrue(metrics.getFalseNegatives() >= 0);
        assertTrue(metrics.getTrueNegatives() >= 0);
        
        // Verificar que las métricas están en rangos válidos
        assertTrue(metrics.getPrecision() >= 0.0 && metrics.getPrecision() <= 1.0);
        assertTrue(metrics.getRecall() >= 0.0 && metrics.getRecall() <= 1.0);
        assertTrue(metrics.getF1Score() >= 0.0 && metrics.getF1Score() <= 1.0);
        assertTrue(metrics.getAccuracy() >= 0.0 && metrics.getAccuracy() <= 1.0);
        
        System.out.println("Consulta individual evaluada:");
        System.out.println("  Precision: " + String.format("%.4f", metrics.getPrecision()));
        System.out.println("  Recall: " + String.format("%.4f", metrics.getRecall()));
        System.out.println("  F1-Score: " + String.format("%.4f", metrics.getF1Score()));
        System.out.println("  Accuracy: " + String.format("%.4f", metrics.getAccuracy()));
    }

    @Test
    @DisplayName("Debería evaluar múltiples consultas y agregar métricas")
    void shouldEvaluateMultipleQueriesAndAggregateMetrics() throws Exception {
        // Given
        List<SearchEvaluator.TestQuery> testQueries = searchEvaluator.createTestQueries();

        // When
        EvaluationMetrics aggregatedMetrics = searchEvaluator.evaluateMultipleQueries(testQueries, 10);

        // Then
        assertNotNull(aggregatedMetrics);
        assertTrue(aggregatedMetrics.getTruePositives() >= 0);
        assertTrue(aggregatedMetrics.getFalsePositives() >= 0);
        assertTrue(aggregatedMetrics.getFalseNegatives() >= 0);
        assertTrue(aggregatedMetrics.getTrueNegatives() >= 0);
        
        // Verificar que las métricas agregadas están en rangos válidos
        assertTrue(aggregatedMetrics.getPrecision() >= 0.0 && aggregatedMetrics.getPrecision() <= 1.0);
        assertTrue(aggregatedMetrics.getRecall() >= 0.0 && aggregatedMetrics.getRecall() <= 1.0);
        assertTrue(aggregatedMetrics.getF1Score() >= 0.0 && aggregatedMetrics.getF1Score() <= 1.0);
        assertTrue(aggregatedMetrics.getAccuracy() >= 0.0 && aggregatedMetrics.getAccuracy() <= 1.0);
        
        System.out.println("\nMétricas agregadas de múltiples consultas:");
        System.out.println("  TP: " + aggregatedMetrics.getTruePositives());
        System.out.println("  FP: " + aggregatedMetrics.getFalsePositives());
        System.out.println("  FN: " + aggregatedMetrics.getFalseNegatives());
        System.out.println("  TN: " + aggregatedMetrics.getTrueNegatives());
        System.out.println("  Precision: " + String.format("%.4f", aggregatedMetrics.getPrecision()));
        System.out.println("  Recall: " + String.format("%.4f", aggregatedMetrics.getRecall()));
        System.out.println("  F1-Score: " + String.format("%.4f", aggregatedMetrics.getF1Score()));
        System.out.println("  Accuracy: " + String.format("%.4f", aggregatedMetrics.getAccuracy()));
    }

    @Test
    @DisplayName("Debería generar reporte de evaluación completo")
    void shouldGenerateCompleteEvaluationReport() throws Exception {
        // Given
        List<SearchEvaluator.TestQuery> testQueries = searchEvaluator.createTestQueries();
        EvaluationMetrics metrics = searchEvaluator.evaluateMultipleQueries(testQueries, 10);

        // When
        String report = searchEvaluator.generateEvaluationReport(metrics, testQueries);

        // Then
        assertNotNull(report);
        assertFalse(report.isEmpty());
        assertTrue(report.contains("REPORTE DE EVALUACIÓN"));
        assertTrue(report.contains("MÉTRICAS DE RENDIMIENTO"));
        assertTrue(report.contains("INTERPRETACIÓN"));
        assertTrue(report.contains("RECOMENDACIONES"));
        
        System.out.println("\n=== REPORTE DE EVALUACIÓN GENERADO ===");
        System.out.println(report);
    }

    @Test
    @DisplayName("Debería manejar consultas con diferentes límites de resultados")
    void shouldHandleQueriesWithDifferentResultLimits() throws Exception {
        // Given
        Set<String> impuestoIds = new HashSet<>();
        impuestoIds.add("ID1302");
        impuestoIds.add("ID1627");
        impuestoIds.add("ID1122");
        impuestoIds.add("ID041");
        impuestoIds.add("ID1902");
        
        SearchEvaluator.TestQuery testQuery = new SearchEvaluator.TestQuery(
            "impuesto",
            impuestoIds,
            "Búsqueda de documentos sobre impuestos"
        );

        // When - Probar con diferentes límites
        EvaluationMetrics metrics5 = searchEvaluator.evaluateSearch(testQuery, 5);
        EvaluationMetrics metrics10 = searchEvaluator.evaluateSearch(testQuery, 10);
        EvaluationMetrics metrics20 = searchEvaluator.evaluateSearch(testQuery, 20);

        // Then
        assertNotNull(metrics5);
        assertNotNull(metrics10);
        assertNotNull(metrics20);
        
        // Con más resultados, generalmente deberíamos tener más TP y FP
        assertTrue(metrics10.getTruePositives() >= metrics5.getTruePositives());
        assertTrue(metrics20.getTruePositives() >= metrics10.getTruePositives());
        
        System.out.println("\nComparación de límites de resultados:");
        System.out.println("  Límite 5:  TP=" + metrics5.getTruePositives() + ", FP=" + metrics5.getFalsePositives());
        System.out.println("  Límite 10: TP=" + metrics10.getTruePositives() + ", FP=" + metrics10.getFalsePositives());
        System.out.println("  Límite 20: TP=" + metrics20.getTruePositives() + ", FP=" + metrics20.getFalsePositives());
    }

    @Test
    @DisplayName("Debería evaluar rendimiento con consultas específicas del dominio")
    void shouldEvaluatePerformanceWithDomainSpecificQueries() throws Exception {
        // Given - Consultas específicas del dominio normativo
        List<SearchEvaluator.TestQuery> domainQueries = new ArrayList<>();
        
        Set<String> ivaIds = new HashSet<>();
        ivaIds.add("ID1302");
        ivaIds.add("ID1627");
        ivaIds.add("ID1122");
        ivaIds.add("ID041");
        domainQueries.add(new SearchEvaluator.TestQuery("IVA", ivaIds, "IVA"));
        
        Set<String> impuestoIds = new HashSet<>();
        impuestoIds.add("ID1302");
        impuestoIds.add("ID1627");
        impuestoIds.add("ID1122");
        impuestoIds.add("ID041");
        impuestoIds.add("ID1902");
        domainQueries.add(new SearchEvaluator.TestQuery("impuesto", impuestoIds, "Impuestos"));
        
        Set<String> servicioIds = new HashSet<>();
        servicioIds.add("ID1302");
        servicioIds.add("ID1627");
        domainQueries.add(new SearchEvaluator.TestQuery("servicio", servicioIds, "Servicios"));
        
        Set<String> tributacionIds = new HashSet<>();
        tributacionIds.add("ID1302");
        tributacionIds.add("ID1902");
        domainQueries.add(new SearchEvaluator.TestQuery("tributación", tributacionIds, "Tributación"));
        
        Set<String> circularIds = new HashSet<>();
        circularIds.add("ID1302");
        circularIds.add("ID1627");
        circularIds.add("ID1122");
        domainQueries.add(new SearchEvaluator.TestQuery("circular", circularIds, "Circulares"));

        // When
        EvaluationMetrics metrics = searchEvaluator.evaluateMultipleQueries(domainQueries, 15);

        // Then
        assertNotNull(metrics);
        
        // Verificar que tenemos resultados válidos
        assertTrue(metrics.getTruePositives() > 0 || metrics.getFalsePositives() > 0);
        
        System.out.println("\n=== EVALUACIÓN DE CONSULTAS DEL DOMINIO ===");
        System.out.println("Consultas evaluadas: " + domainQueries.size());
        System.out.println("Total documentos en índice: " + "N/A (campo privado)");
        System.out.println("Métricas finales:");
        System.out.println("  Precision: " + String.format("%.4f", metrics.getPrecision()));
        System.out.println("  Recall: " + String.format("%.4f", metrics.getRecall()));
        System.out.println("  F1-Score: " + String.format("%.4f", metrics.getF1Score()));
        System.out.println("  Accuracy: " + String.format("%.4f", metrics.getAccuracy()));
        
        // Generar reporte específico del dominio
        String domainReport = searchEvaluator.generateEvaluationReport(metrics, domainQueries);
        System.out.println("\n" + domainReport);
    }

    @Test
    @DisplayName("Debería comparar rendimiento entre diferentes tipos de consultas")
    void shouldComparePerformanceBetweenDifferentQueryTypes() throws Exception {
        // Given - Diferentes tipos de consultas
        Set<String> singleTermIds = new HashSet<>();
        singleTermIds.add("ID1302");
        singleTermIds.add("ID1627");
        singleTermIds.add("ID1122");
        singleTermIds.add("ID041");
        
        Set<String> multiTermIds = new HashSet<>();
        multiTermIds.add("ID1302");
        multiTermIds.add("ID1627");
        multiTermIds.add("ID1122");
        multiTermIds.add("ID041");
        
        Set<String> phraseIds = new HashSet<>();
        phraseIds.add("ID1627");
        
        SearchEvaluator.TestQuery singleTerm = new SearchEvaluator.TestQuery(
            "IVA", singleTermIds, "Término único"
        );
        
        SearchEvaluator.TestQuery multiTerm = new SearchEvaluator.TestQuery(
            "IVA impuesto", multiTermIds, "Múltiples términos"
        );
        
        SearchEvaluator.TestQuery phraseQuery = new SearchEvaluator.TestQuery(
            "\"impuesto a las ventas\"", phraseIds, "Consulta por frase"
        );

        // When
        EvaluationMetrics singleMetrics = searchEvaluator.evaluateSearch(singleTerm, 10);
        EvaluationMetrics multiMetrics = searchEvaluator.evaluateSearch(multiTerm, 10);
        EvaluationMetrics phraseMetrics = searchEvaluator.evaluateSearch(phraseQuery, 10);

        // Then
        assertNotNull(singleMetrics);
        assertNotNull(multiMetrics);
        assertNotNull(phraseMetrics);
        
        System.out.println("\n=== COMPARACIÓN DE TIPOS DE CONSULTAS ===");
        System.out.println("Término único:");
        System.out.println("  Precision: " + String.format("%.4f", singleMetrics.getPrecision()));
        System.out.println("  Recall: " + String.format("%.4f", singleMetrics.getRecall()));
        System.out.println("  F1-Score: " + String.format("%.4f", singleMetrics.getF1Score()));
        
        System.out.println("Múltiples términos:");
        System.out.println("  Precision: " + String.format("%.4f", multiMetrics.getPrecision()));
        System.out.println("  Recall: " + String.format("%.4f", multiMetrics.getRecall()));
        System.out.println("  F1-Score: " + String.format("%.4f", multiMetrics.getF1Score()));
        
        System.out.println("Consulta por frase:");
        System.out.println("  Precision: " + String.format("%.4f", phraseMetrics.getPrecision()));
        System.out.println("  Recall: " + String.format("%.4f", phraseMetrics.getRecall()));
        System.out.println("  F1-Score: " + String.format("%.4f", phraseMetrics.getF1Score()));
    }

    @Test
    @DisplayName("Debería validar consistencia de métricas en múltiples ejecuciones")
    void shouldValidateMetricsConsistencyAcrossMultipleRuns() throws Exception {
        // Given
        Set<String> consistencyIds = new HashSet<>();
        consistencyIds.add("ID1302");
        consistencyIds.add("ID1627");
        consistencyIds.add("ID1122");
        consistencyIds.add("ID041");
        
        SearchEvaluator.TestQuery testQuery = new SearchEvaluator.TestQuery(
            "IVA",
            consistencyIds,
            "Consulta de consistencia"
        );

        // When - Ejecutar múltiples veces
        EvaluationMetrics metrics1 = searchEvaluator.evaluateSearch(testQuery, 10);
        EvaluationMetrics metrics2 = searchEvaluator.evaluateSearch(testQuery, 10);
        EvaluationMetrics metrics3 = searchEvaluator.evaluateSearch(testQuery, 10);

        // Then - Las métricas deberían ser consistentes
        assertEquals(metrics1.getTruePositives(), metrics2.getTruePositives());
        assertEquals(metrics2.getTruePositives(), metrics3.getTruePositives());
        
        assertEquals(metrics1.getFalsePositives(), metrics2.getFalsePositives());
        assertEquals(metrics2.getFalsePositives(), metrics3.getFalsePositives());
        
        assertEquals(metrics1.getFalseNegatives(), metrics2.getFalseNegatives());
        assertEquals(metrics2.getFalseNegatives(), metrics3.getFalseNegatives());
        
        assertEquals(metrics1.getTrueNegatives(), metrics2.getTrueNegatives());
        assertEquals(metrics2.getTrueNegatives(), metrics3.getTrueNegatives());
        
        System.out.println("\n=== VALIDACIÓN DE CONSISTENCIA ===");
        System.out.println("Métricas consistentes en múltiples ejecuciones:");
        System.out.println("  TP: " + metrics1.getTruePositives());
        System.out.println("  FP: " + metrics1.getFalsePositives());
        System.out.println("  FN: " + metrics1.getFalseNegatives());
        System.out.println("  TN: " + metrics1.getTrueNegatives());
    }
}
