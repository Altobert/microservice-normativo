package cl.sii.normativo.loadnormas.services;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Servicio de búsqueda híbrida que combina búsqueda tradicional y vectorial.
 * 
 * Este servicio proporciona:
 * - Búsqueda tradicional por términos exactos
 * - Búsqueda vectorial por similitud semántica
 * - Combinación inteligente de ambos resultados
 * - Scoring híbrido optimizado
 */
@Service
public class HybridSearchService {

    @Autowired
    private EmbeddingService embeddingService;

    @Value("${lucene.index.directory:path/to/index}")
    private String indexDir;

    private static final String VECTOR_FIELD = "vector";
    private static final String CONTENT_FIELD = "content";
    private static final String FILENAME_FIELD = "filename";
    private static final String DOCUMENT_ID_FIELD = "documentId";
    private static final String YEAR_FIELD = "year";
    private static final String TITLE_FIELD = "title";

    /**
     * Resultado de búsqueda híbrida con información detallada.
     */
    public static class HybridSearchResult {
        private String documentId;
        private String filename;
        private String title;
        private String content;
        private String year;
        private double traditionalScore;
        private double vectorScore;
        private double hybridScore;
        private String matchType; // "traditional", "vector", "hybrid"

        public HybridSearchResult(String documentId, String filename, String title, 
                                String content, String year, double traditionalScore, 
                                double vectorScore, double hybridScore, String matchType) {
            this.documentId = documentId;
            this.filename = filename;
            this.title = title;
            this.content = content;
            this.year = year;
            this.traditionalScore = traditionalScore;
            this.vectorScore = vectorScore;
            this.hybridScore = hybridScore;
            this.matchType = matchType;
        }

        // Getters
        public String getDocumentId() { return documentId; }
        public String getFilename() { return filename; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getYear() { return year; }
        public double getTraditionalScore() { return traditionalScore; }
        public double getVectorScore() { return vectorScore; }
        public double getHybridScore() { return hybridScore; }
        public String getMatchType() { return matchType; }
    }

    /**
     * Realiza búsqueda híbrida combinando tradicional y vectorial.
     * 
     * @param queryString Consulta de búsqueda
     * @param limit Límite de resultados
     * @param traditionalWeight Peso para búsqueda tradicional (0.0-1.0)
     * @param vectorWeight Peso para búsqueda vectorial (0.0-1.0)
     * @return Lista de resultados híbridos ordenados por score
     */
    public List<HybridSearchResult> hybridSearch(String queryString, int limit, 
                                                double traditionalWeight, double vectorWeight) 
                                                throws IOException, ParseException {
        
        System.out.println("Ejecutando búsqueda híbrida: '" + queryString + "'");
        System.out.println("Pesos - Tradicional: " + traditionalWeight + ", Vectorial: " + vectorWeight);
        
        // Normalizar pesos
        double totalWeight = traditionalWeight + vectorWeight;
        if (totalWeight > 0) {
            traditionalWeight /= totalWeight;
            vectorWeight /= totalWeight;
        }
        
        List<HybridSearchResult> results = new ArrayList<>();
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             DirectoryReader reader = DirectoryReader.open(dir)) {
            
            IndexSearcher searcher = new IndexSearcher(reader);
            
            // 1. Búsqueda tradicional
            List<HybridSearchResult> traditionalResults = performTraditionalSearch(
                searcher, queryString, limit * 2); // Obtener más resultados para mejor combinación
            
            // 2. Búsqueda vectorial
            List<HybridSearchResult> vectorResults = performVectorSearch(
                searcher, queryString, limit * 2);
            
            // 3. Combinar resultados
            results = combineSearchResults(traditionalResults, vectorResults, 
                                         traditionalWeight, vectorWeight, limit);
            
            System.out.println("Búsqueda híbrida completada - " + results.size() + " resultados");
        }
        
        return results;
    }

    /**
     * Realiza búsqueda tradicional por términos.
     */
    private List<HybridSearchResult> performTraditionalSearch(IndexSearcher searcher, 
                                                             String queryString, int limit) 
                                                             throws ParseException, IOException {
        
        QueryParser parser = new QueryParser(CONTENT_FIELD, new StandardAnalyzer());
        Query query = parser.parse(queryString);
        
        TopDocs topDocs = searcher.search(query, limit);
        
        List<HybridSearchResult> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            
            HybridSearchResult result = new HybridSearchResult(
                doc.get(DOCUMENT_ID_FIELD),
                doc.get(FILENAME_FIELD),
                doc.get(TITLE_FIELD),
                doc.get(CONTENT_FIELD),
                doc.get(YEAR_FIELD),
                scoreDoc.score,
                0.0, // No hay score vectorial
                0.0, // Se calculará después
                "traditional"
            );
            results.add(result);
        }
        
        return results;
    }

    /**
     * Realiza búsqueda vectorial por similitud semántica.
     */
    private List<HybridSearchResult> performVectorSearch(IndexSearcher searcher, 
                                                        String queryString, int limit) 
                                                        throws IOException {
        
        // Generar embedding para la consulta
        Map<String, Double> queryEmbedding = embeddingService.generateEmbedding(queryString);
        
        // Crear query vectorial usando similitud de texto
        Query vectorQuery = new org.apache.lucene.search.TermQuery(
            new org.apache.lucene.index.Term(VECTOR_FIELD, serializeEmbedding(queryEmbedding))
        );
        
        TopDocs topDocs = searcher.search(vectorQuery, limit);
        
        List<HybridSearchResult> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            
            HybridSearchResult result = new HybridSearchResult(
                doc.get(DOCUMENT_ID_FIELD),
                doc.get(FILENAME_FIELD),
                doc.get(TITLE_FIELD),
                doc.get(CONTENT_FIELD),
                doc.get(YEAR_FIELD),
                0.0, // No hay score tradicional
                scoreDoc.score,
                0.0, // Se calculará después
                "vector"
            );
            results.add(result);
        }
        
        return results;
    }

    /**
     * Combina resultados de búsqueda tradicional y vectorial.
     */
    private List<HybridSearchResult> combineSearchResults(List<HybridSearchResult> traditionalResults,
                                                        List<HybridSearchResult> vectorResults,
                                                        double traditionalWeight,
                                                        double vectorWeight,
                                                        int limit) {
        
        // Crear mapa para combinar resultados por documentId
        Map<String, HybridSearchResult> combinedMap = new HashMap<>();
        
        // Agregar resultados tradicionales
        for (HybridSearchResult result : traditionalResults) {
            combinedMap.put(result.getDocumentId(), result);
        }
        
        // Combinar con resultados vectoriales
        for (HybridSearchResult vectorResult : vectorResults) {
            String docId = vectorResult.getDocumentId();
            
            if (combinedMap.containsKey(docId)) {
                // Documento existe en ambos resultados - combinar scores
                HybridSearchResult existing = combinedMap.get(docId);
                double hybridScore = (existing.getTraditionalScore() * traditionalWeight) + 
                                   (vectorResult.getVectorScore() * vectorWeight);
                
                HybridSearchResult combined = new HybridSearchResult(
                    existing.getDocumentId(),
                    existing.getFilename(),
                    existing.getTitle(),
                    existing.getContent(),
                    existing.getYear(),
                    existing.getTraditionalScore(),
                    vectorResult.getVectorScore(),
                    hybridScore,
                    "hybrid"
                );
                combinedMap.put(docId, combined);
            } else {
                // Solo existe en resultados vectoriales
                double hybridScore = vectorResult.getVectorScore() * vectorWeight;
                HybridSearchResult combined = new HybridSearchResult(
                    vectorResult.getDocumentId(),
                    vectorResult.getFilename(),
                    vectorResult.getTitle(),
                    vectorResult.getContent(),
                    vectorResult.getYear(),
                    0.0,
                    vectorResult.getVectorScore(),
                    hybridScore,
                    "vector"
                );
                combinedMap.put(docId, combined);
            }
        }
        
        // Convertir a lista y ordenar por score híbrido
        List<HybridSearchResult> finalResults = new ArrayList<>(combinedMap.values());
        finalResults.sort((a, b) -> Double.compare(b.getHybridScore(), a.getHybridScore()));
        
        // Limitar resultados
        return finalResults.stream().limit(limit).toList();
    }

    /**
     * Búsqueda híbrida con pesos automáticos basados en el tipo de consulta.
     * 
     * @param queryString Consulta de búsqueda
     * @param limit Límite de resultados
     * @return Lista de resultados híbridos
     */
    public List<HybridSearchResult> smartHybridSearch(String queryString, int limit) 
            throws IOException, ParseException {
        
        // Determinar pesos automáticamente basado en características de la consulta
        double traditionalWeight = 0.6; // Peso por defecto para tradicional
        double vectorWeight = 0.4;      // Peso por defecto para vectorial
        
        // Ajustar pesos basado en características de la consulta
        if (queryString.length() > 50) {
            // Consultas largas tienden a ser más semánticas
            traditionalWeight = 0.4;
            vectorWeight = 0.6;
        } else if (queryString.matches(".*\\b(\\w{1,3})\\b.*")) {
            // Consultas con palabras muy cortas tienden a ser más exactas
            traditionalWeight = 0.7;
            vectorWeight = 0.3;
        }
        
        return hybridSearch(queryString, limit, traditionalWeight, vectorWeight);
    }

    /**
     * Obtiene estadísticas del servicio de búsqueda híbrida.
     */
    public Map<String, Object> getSearchStats() {
        return Map.of(
            "indexDirectory", indexDir,
            "embeddingServiceAvailable", embeddingService.isAvailable(),
            "embeddingCacheSize", embeddingService.getCacheSize(),
            "searchType", "hybrid",
            "supportedFields", Arrays.asList(CONTENT_FIELD, VECTOR_FIELD, FILENAME_FIELD, TITLE_FIELD)
        );
    }

    /**
     * Serializa un embedding a string para almacenamiento en Lucene.
     * 
     * @param embedding El embedding a serializar
     * @return String serializado del embedding
     */
    private String serializeEmbedding(Map<String, Double> embedding) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : embedding.entrySet()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }
}
