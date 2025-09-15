package cl.sii.normativo.loadnormas.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    
    @Value("${lucene.index.directory:path/to/index}")
    private String indexDir;
    
    /**
     * Busca documentos en el índice de Lucene
     */
    @GetMapping("/documents")
    public ResponseEntity<Map<String, Object>> searchDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "content") String field) {
        
        try {
            List<Map<String, Object>> results = performSearch(query, limit, field);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("field", field);
            response.put("limit", limit);
            response.put("totalResults", results.size());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error en la búsqueda");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Busca documentos por año específico
     */
    @GetMapping("/documents/year/{year}")
    public ResponseEntity<Map<String, Object>> searchDocumentsByYear(
            @PathVariable String year,
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            String yearQuery = query + " AND year:" + year;
            List<Map<String, Object>> results = performSearch(yearQuery, limit, "content");
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("year", year);
            response.put("limit", limit);
            response.put("totalResults", results.size());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error en la búsqueda por año");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Busca documentos por ID específico
     */
    @GetMapping("/documents/id/{documentId}")
    public ResponseEntity<Map<String, Object>> searchDocumentById(@PathVariable String documentId) {
        
        try {
            String query = "documentId:" + documentId;
            List<Map<String, Object>> results = performSearch(query, 1, "documentId");
            
            Map<String, Object> response = new HashMap<>();
            response.put("documentId", documentId);
            response.put("totalResults", results.size());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error en la búsqueda por ID");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Realiza la búsqueda en el índice de Lucene
     */
    private List<Map<String, Object>> performSearch(String queryString, int limit, String field) 
            throws IOException, ParseException {
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             DirectoryReader reader = DirectoryReader.open(dir)) {
            
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser(field, new StandardAnalyzer());
            Query query = parser.parse(queryString);
            
            TopDocs topDocs = searcher.search(query, limit);
            
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                
                Map<String, Object> result = new HashMap<>();
                result.put("score", scoreDoc.score);
                result.put("filename", doc.get("filename"));
                result.put("title", doc.get("title"));
                result.put("filepath", doc.get("filepath"));
                result.put("year", doc.get("year"));
                result.put("documentId", doc.get("documentId"));
                result.put("size", doc.get("size"));
                result.put("lastModified", doc.get("lastModified"));
                
                // Mostrar snippet del contenido (primeros 200 caracteres)
                String content = doc.get("content");
                if (content != null && content.length() > 200) {
                    result.put("snippet", content.substring(0, 200) + "...");
                } else {
                    result.put("snippet", content);
                }
                
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * Obtiene estadísticas del índice
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getIndexStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            try (Directory dir = FSDirectory.open(Paths.get(indexDir));
                 DirectoryReader reader = DirectoryReader.open(dir)) {
                
                stats.put("totalDocuments", reader.numDocs());
                stats.put("indexDirectory", indexDir);
                stats.put("timestamp", System.currentTimeMillis());
                
                // Contar documentos por año
                Map<String, Integer> yearCount = new HashMap<>();
                for (int i = 0; i < reader.numDocs(); i++) {
                    Document doc = reader.document(i);
                    String year = doc.get("year");
                    if (year != null) {
                        yearCount.put(year, yearCount.getOrDefault(year, 0) + 1);
                    }
                }
                stats.put("documentsByYear", yearCount);
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener estadísticas");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}