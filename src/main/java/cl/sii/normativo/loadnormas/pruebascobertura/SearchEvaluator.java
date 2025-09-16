package cl.sii.normativo.loadnormas.pruebascobertura;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Evaluador de rendimiento del sistema de búsqueda utilizando EvaluationMetrics.
 * 
 * Esta clase permite evaluar la calidad de los resultados de búsqueda
 * comparándolos con resultados esperados (ground truth).
 */
@Component
public class SearchEvaluator {

    @Value("${lucene.index.directory:path/to/index}")
    private String indexDir;

    private IndexSearcher searcher;
    private QueryParser queryParser;

    /**
     * Inicializa el evaluador con el índice de Lucene.
     */
    public void initialize() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        DirectoryReader reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);
        queryParser = new QueryParser("content", new StandardAnalyzer());
    }

    /**
     * Resultado de una búsqueda individual.
     */
    public static class SearchResult {
        private String documentId;
        private String filename;
        private String title;
        private double score;
        private boolean isRelevant;

        public SearchResult(String documentId, String filename, String title, double score, boolean isRelevant) {
            this.documentId = documentId;
            this.filename = filename;
            this.title = title;
            this.score = score;
            this.isRelevant = isRelevant;
        }

        // Getters
        public String getDocumentId() { return documentId; }
        public String getFilename() { return filename; }
        public String getTitle() { return title; }
        public double getScore() { return score; }
        public boolean isRelevant() { return isRelevant; }
    }

    /**
     * Conjunto de datos de prueba con consultas y resultados esperados.
     */
    public static class TestQuery {
        private String query;
        private Set<String> relevantDocumentIds;
        private String description;

        public TestQuery(String query, Set<String> relevantDocumentIds, String description) {
            this.query = query;
            this.relevantDocumentIds = relevantDocumentIds;
            this.description = description;
        }

        // Getters
        public String getQuery() { return query; }
        public Set<String> getRelevantDocumentIds() { return relevantDocumentIds; }
        public String getDescription() { return description; }
    }

    /**
     * Realiza una búsqueda en el índice y evalúa los resultados.
     */
    public EvaluationMetrics evaluateSearch(TestQuery testQuery, int maxResults) throws IOException, ParseException {
        EvaluationMetrics metrics = new EvaluationMetrics();

        // Realizar búsqueda
        Query query = queryParser.parse(testQuery.getQuery());
        TopDocs topDocs = searcher.search(query, maxResults);

        // Obtener IDs de documentos encontrados
        Set<String> foundDocumentIds = new HashSet<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.storedFields().document(scoreDoc.doc);
            String documentId = doc.get("documentId");
            if (documentId != null) {
                foundDocumentIds.add(documentId);
            }
        }

        // Calcular métricas
        Set<String> relevantIds = testQuery.getRelevantDocumentIds();
        
        // True Positives: Documentos relevantes encontrados
        for (String docId : foundDocumentIds) {
            if (relevantIds.contains(docId)) {
                metrics.incrementTruePositives();
            } else {
                metrics.incrementFalsePositives();
            }
        }

        // False Negatives: Documentos relevantes no encontrados
        for (String docId : relevantIds) {
            if (!foundDocumentIds.contains(docId)) {
                metrics.incrementFalseNegatives();
            }
        }

        // True Negatives: Documentos no relevantes no encontrados
        // Para simplificar, asumimos que hay un conjunto total de documentos
        // y calculamos TN como: Total - TP - FP - FN
        int totalDocuments = searcher.getIndexReader().numDocs();
        int tn = totalDocuments - metrics.getTruePositives() - metrics.getFalsePositives() - metrics.getFalseNegatives();
        for (int i = 0; i < tn; i++) {
            metrics.incrementTrueNegatives();
        }

        return metrics;
    }

    /**
     * Evalúa múltiples consultas y retorna métricas agregadas.
     */
    public EvaluationMetrics evaluateMultipleQueries(List<TestQuery> testQueries, int maxResults) throws IOException, ParseException {
        EvaluationMetrics aggregatedMetrics = new EvaluationMetrics();

        for (TestQuery testQuery : testQueries) {
            EvaluationMetrics queryMetrics = evaluateSearch(testQuery, maxResults);
            
            // Agregar métricas individuales a las agregadas
            for (int i = 0; i < queryMetrics.getTruePositives(); i++) {
                aggregatedMetrics.incrementTruePositives();
            }
            for (int i = 0; i < queryMetrics.getFalsePositives(); i++) {
                aggregatedMetrics.incrementFalsePositives();
            }
            for (int i = 0; i < queryMetrics.getFalseNegatives(); i++) {
                aggregatedMetrics.incrementFalseNegatives();
            }
            for (int i = 0; i < queryMetrics.getTrueNegatives(); i++) {
                aggregatedMetrics.incrementTrueNegatives();
            }
        }

        return aggregatedMetrics;
    }

    /**
     * Crea un conjunto de consultas de prueba basadas en documentos conocidos.
     */
    public List<TestQuery> createTestQueries() {
        List<TestQuery> testQueries = new ArrayList<>();

        // Consulta 1: IVA
        Set<String> ivaRelevant = new HashSet<>();
        ivaRelevant.add("ID1302"); // Tributación Régimen ADM IVA Servicios Extranjeros
        ivaRelevant.add("ID1627"); // Instrucciones Modificaciones Generales Ley Impuesto Ventas
        ivaRelevant.add("ID1122"); // Instrucciones Modificación Art 64 DL 825 1974
        ivaRelevant.add("ID041");  // Ley 20 899 Modifica Ley IVA
        testQueries.add(new TestQuery("IVA", ivaRelevant, "Búsqueda de documentos sobre IVA"));

        // Consulta 2: Impuesto
        Set<String> impuestoRelevant = new HashSet<>();
        impuestoRelevant.add("ID1302");
        impuestoRelevant.add("ID1627");
        impuestoRelevant.add("ID1122");
        impuestoRelevant.add("ID041");
        impuestoRelevant.add("ID1902"); // Tributación Comercialización Derechos Autor
        testQueries.add(new TestQuery("impuesto", impuestoRelevant, "Búsqueda de documentos sobre impuestos"));

        // Consulta 3: Servicio
        Set<String> servicioRelevant = new HashSet<>();
        servicioRelevant.add("ID1302"); // Servicios Extranjeros
        servicioRelevant.add("ID1627"); // Servicios
        testQueries.add(new TestQuery("servicio", servicioRelevant, "Búsqueda de documentos sobre servicios"));

        // Consulta 4: Tributación
        Set<String> tributacionRelevant = new HashSet<>();
        tributacionRelevant.add("ID1302"); // Tributación
        tributacionRelevant.add("ID1902"); // Tributación
        testQueries.add(new TestQuery("tributación", tributacionRelevant, "Búsqueda de documentos sobre tributación"));

        // Consulta 5: Circular
        Set<String> circularRelevant = new HashSet<>();
        circularRelevant.add("ID1302"); // Circular
        circularRelevant.add("ID1627"); // Circular
        circularRelevant.add("ID1122"); // Circular
        testQueries.add(new TestQuery("circular", circularRelevant, "Búsqueda de documentos tipo circular"));

        return testQueries;
    }

    /**
     * Genera un reporte detallado de evaluación.
     */
    public String generateEvaluationReport(EvaluationMetrics metrics, List<TestQuery> testQueries) {
        StringBuilder report = new StringBuilder();
        
        report.append("=== REPORTE DE EVALUACIÓN DEL SISTEMA DE BÚSQUEDA ===\n\n");
        
        report.append("📊 RESUMEN DE MÉTRICAS:\n");
        report.append("  • True Positives (TP):  ").append(metrics.getTruePositives()).append("\n");
        report.append("  • False Positives (FP): ").append(metrics.getFalsePositives()).append("\n");
        report.append("  • False Negatives (FN): ").append(metrics.getFalseNegatives()).append("\n");
        report.append("  • True Negatives (TN):  ").append(metrics.getTrueNegatives()).append("\n\n");
        
        report.append("📈 MÉTRICAS DE RENDIMIENTO:\n");
        report.append(String.format("  • Precision: %.4f (%.2f%%)\n", 
                     metrics.getPrecision(), metrics.getPrecision() * 100));
        report.append(String.format("  • Recall:     %.4f (%.2f%%)\n", 
                     metrics.getRecall(), metrics.getRecall() * 100));
        report.append(String.format("  • F1-Score:   %.4f (%.2f%%)\n", 
                     metrics.getF1Score(), metrics.getF1Score() * 100));
        report.append(String.format("  • Accuracy:   %.4f (%.2f%%)\n", 
                     metrics.getAccuracy(), metrics.getAccuracy() * 100));
        
        report.append("\n💡 INTERPRETACIÓN:\n");
        interpretMetrics(metrics, report);
        
        report.append("\n📋 CONSULTAS EVALUADAS:\n");
        for (int i = 0; i < testQueries.size(); i++) {
            TestQuery query = testQueries.get(i);
            report.append(String.format("  %d. \"%s\" - %s\n", 
                         i + 1, query.getQuery(), query.getDescription()));
            report.append(String.format("     Documentos relevantes esperados: %d\n", 
                         query.getRelevantDocumentIds().size()));
        }
        
        report.append("\n🎯 RECOMENDACIONES:\n");
        generateRecommendations(metrics, report);
        
        return report.toString();
    }

    /**
     * Interpreta las métricas y proporciona análisis.
     */
    private void interpretMetrics(EvaluationMetrics metrics, StringBuilder report) {
        double precision = metrics.getPrecision();
        double recall = metrics.getRecall();
        double f1Score = metrics.getF1Score();
        double accuracy = metrics.getAccuracy();

        // Análisis de Precision
        if (precision >= 0.9) {
            report.append("  • Precision excelente: El sistema retorna muy pocos resultados irrelevantes\n");
        } else if (precision >= 0.7) {
            report.append("  • Precision buena: El sistema retorna pocos resultados irrelevantes\n");
        } else if (precision >= 0.5) {
            report.append("  • Precision moderada: El sistema retorna algunos resultados irrelevantes\n");
        } else {
            report.append("  • Precision baja: El sistema retorna muchos resultados irrelevantes\n");
        }

        // Análisis de Recall
        if (recall >= 0.9) {
            report.append("  • Recall excelente: El sistema encuentra casi todos los documentos relevantes\n");
        } else if (recall >= 0.7) {
            report.append("  • Recall bueno: El sistema encuentra la mayoría de documentos relevantes\n");
        } else if (recall >= 0.5) {
            report.append("  • Recall moderado: El sistema encuentra algunos documentos relevantes\n");
        } else {
            report.append("  • Recall bajo: El sistema pierde muchos documentos relevantes\n");
        }

        // Análisis de F1-Score
        if (f1Score >= 0.9) {
            report.append("  • F1-Score excelente: Balance perfecto entre precision y recall\n");
        } else if (f1Score >= 0.7) {
            report.append("  • F1-Score bueno: Buen balance entre precision y recall\n");
        } else if (f1Score >= 0.5) {
            report.append("  • F1-Score moderado: Balance aceptable\n");
        } else {
            report.append("  • F1-Score bajo: Necesita mejora en precision o recall\n");
        }

        // Análisis de Accuracy
        if (accuracy >= 0.9) {
            report.append("  • Accuracy excelente: Sistema muy preciso en general\n");
        } else if (accuracy >= 0.7) {
            report.append("  • Accuracy bueno: Sistema preciso en general\n");
        } else if (accuracy >= 0.5) {
            report.append("  • Accuracy moderado: Sistema aceptable\n");
        } else {
            report.append("  • Accuracy bajo: Sistema necesita mejoras significativas\n");
        }
    }

    /**
     * Genera recomendaciones basadas en las métricas.
     */
    private void generateRecommendations(EvaluationMetrics metrics, StringBuilder report) {
        double precision = metrics.getPrecision();
        double recall = metrics.getRecall();

        if (precision < 0.7 && recall >= 0.7) {
            report.append("  • Mejorar filtrado de resultados para reducir false positives\n");
            report.append("  • Considerar ajustar umbrales de relevancia\n");
        } else if (precision >= 0.7 && recall < 0.7) {
            report.append("  • Mejorar cobertura de búsqueda para aumentar recall\n");
            report.append("  • Considerar expansión de consultas o sinónimos\n");
        } else if (precision < 0.7 && recall < 0.7) {
            report.append("  • Revisar algoritmo de búsqueda completo\n");
            report.append("  • Considerar mejoras en indexación y análisis de texto\n");
        } else {
            report.append("  • Sistema funcionando bien, mantener configuración actual\n");
            report.append("  • Monitorear rendimiento en nuevas consultas\n");
        }

        if (metrics.getF1Score() < 0.6) {
            report.append("  • Implementar técnicas de aprendizaje automático para mejorar resultados\n");
            report.append("  • Analizar consultas que fallan más frecuentemente\n");
        }
    }

    /**
     * Cierra recursos del evaluador.
     */
    public void close() throws IOException {
        if (searcher != null && searcher.getIndexReader() != null) {
            searcher.getIndexReader().close();
        }
    }
}
