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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Búsqueda de Documentos", description = "API para realizar búsquedas en el índice de documentos normativos")
public class SearchController {
    
    @Value("${lucene.index.directory:path/to/index}")
    private String indexDir;
    
    /**
     * Busca documentos en el índice de Lucene
     */
    @Operation(
        summary = "Buscar documentos",
        description = "Realiza una búsqueda de texto completo en el índice de documentos normativos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Búsqueda realizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "query": "IVA",
                      "field": "content",
                      "limit": 10,
                      "totalResults": 2,
                      "results": [
                        {
                          "score": 1.2345955,
                          "filename": "documento.pdf",
                          "title": "Título del documento",
                          "filepath": "/ruta/al/archivo.pdf",
                          "year": "2020",
                          "documentId": "ID1302",
                          "size": "563916",
                          "lastModified": "1728443774000",
                          "snippet": "Contenido del documento..."
                        }
                      ]
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error en la búsqueda",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "error": "Error en la búsqueda",
                      "message": "Mensaje de error específico"
                    }
                    """)
            )
        )
    })
    @GetMapping("/documents")
    public ResponseEntity<Map<String, Object>> searchDocuments(
            @Parameter(description = "Término de búsqueda", required = true, example = "IVA")
            @RequestParam String query,
            @Parameter(description = "Número máximo de resultados", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Campo a buscar", example = "content")
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
    @Operation(
        summary = "Obtener estadísticas del índice",
        description = "Retorna información estadística sobre los documentos indexados en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Estadísticas obtenidas exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "totalDocuments": 1170,
                      "indexDirectory": "path/to/index",
                      "timestamp": 1757910350026,
                      "documentsByYear": {
                        "2019": 44,
                        "2018": 55,
                        "2017": 77,
                        "2016": 352,
                        "2024": 66,
                        "2021": 242,
                        "2020": 308
                      }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error al obtener estadísticas",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "error": "Error al obtener estadísticas",
                      "message": "Mensaje de error específico"
                    }
                    """)
            )
        )
    })
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