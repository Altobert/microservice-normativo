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

import lombok.extern.slf4j.Slf4j;

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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Búsqueda de Documentos", description = "API para realizar búsquedas en el índice de documentos normativos")
@Slf4j
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
        
        log.info("Iniciando búsqueda de documentos - Query: '{}', Campo: '{}', Límite: {}", query, field, limit);
        
        try {
            List<Map<String, Object>> results = performSearch(query, limit, field);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("field", field);
            response.put("limit", limit);
            response.put("totalResults", results.size());
            response.put("results", results);
            
            log.info("Búsqueda completada exitosamente - {} resultados encontrados para query: '{}'", results.size(), query);
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            log.error("Error durante la búsqueda de documentos - Query: '{}', Campo: '{}', Límite: {}", 
                     query, field, limit, e);
            return createErrorResponse("Error en la búsqueda", e.getMessage());
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
        
        log.info("Iniciando búsqueda por año - Query: '{}', Año: '{}', Límite: {}", query, year, limit);
        
        try {
            String yearQuery = query + " AND year:" + year;
            List<Map<String, Object>> results = performSearch(yearQuery, limit, "content");
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("year", year);
            response.put("limit", limit);
            response.put("totalResults", results.size());
            response.put("results", results);
            
            log.info("Búsqueda por año completada - {} resultados encontrados para query: '{}' en año: '{}'", results.size(), query, year);
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            log.error("Error durante la búsqueda por año - Query: '{}', Año: '{}', Límite: {}", 
                     query, year, limit, e);
            return createErrorResponse("Error en la búsqueda por año", e.getMessage());
        }
    }
    
    /**
     * Busca documentos por ID específico
     */
    @GetMapping("/documents/id/{documentId}")
    public ResponseEntity<Map<String, Object>> searchDocumentById(@PathVariable String documentId) {
        
        log.info("Iniciando búsqueda por ID de documento - DocumentId: '{}'", documentId);
        
        try {
            String query = "documentId:" + documentId;
            List<Map<String, Object>> results = performSearch(query, 1, "documentId");
            
            Map<String, Object> response = new HashMap<>();
            response.put("documentId", documentId);
            response.put("totalResults", results.size());
            response.put("results", results);
            
            log.info("Búsqueda por ID completada - {} resultados encontrados para DocumentId: '{}'", results.size(), documentId);
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            log.error("Error durante la búsqueda por ID - DocumentId: '{}'", documentId, e);
            return createErrorResponse("Error en la búsqueda por ID", e.getMessage());
        }
    }
    
    /**
     * Realiza la búsqueda en el índice de Lucene
     */
    private List<Map<String, Object>> performSearch(String queryString, int limit, String field) 
            throws IOException, ParseException {
        
        log.debug("Ejecutando búsqueda en Lucene - Query: '{}', Campo: '{}', Límite: {}", queryString, field, limit);
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             DirectoryReader reader = DirectoryReader.open(dir)) {
            
            log.debug("Directorio de índice abierto: {}", indexDir);
            log.debug("Número total de documentos en el índice: {}", reader.numDocs());
            
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser(field, new StandardAnalyzer());
            Query query = parser.parse(queryString);
            
            TopDocs topDocs = searcher.search(query, limit);
            log.debug("Búsqueda ejecutada - {} documentos encontrados", topDocs.totalHits.value);
            
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.storedFields().document(scoreDoc.doc);
                
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
                log.debug("Documento procesado - ID: {}, Score: {}, Filename: {}", 
                         doc.get("documentId"), scoreDoc.score, doc.get("filename"));
            }
        }
        
        log.debug("Búsqueda completada - {} resultados procesados", results.size());
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
        log.info("Solicitando estadísticas del índice");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            try (Directory dir = FSDirectory.open(Paths.get(indexDir));
                 DirectoryReader reader = DirectoryReader.open(dir)) {
                
                int totalDocs = reader.numDocs();
                log.debug("Procesando estadísticas - Total documentos: {}", totalDocs);
                
                stats.put("totalDocuments", totalDocs);
                stats.put("indexDirectory", indexDir);
                stats.put("timestamp", System.currentTimeMillis());
                
                // Contar documentos por año
                Map<String, Integer> yearCount = new HashMap<>();
                for (int i = 0; i < totalDocs; i++) {
                    Document doc = reader.storedFields().document(i);
                    String year = doc.get("year");
                    if (year != null) {
                        yearCount.put(year, yearCount.getOrDefault(year, 0) + 1);
                    }
                }
                stats.put("documentsByYear", yearCount);
                
                log.info("Estadísticas generadas exitosamente - {} documentos totales, {} años diferentes", 
                        totalDocs, yearCount.size());
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (IOException e) {
            log.error("Error al obtener estadísticas del índice", e);
            return createErrorResponse("Error al obtener estadísticas", e.getMessage());
        }
    }
    
    /**
     * Crea una respuesta de error estandarizada usando SLF4J
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String errorType, String message) {
        log.debug("Creando respuesta de error - Tipo: '{}', Mensaje: '{}'", errorType, message);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorType);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(500).body(errorResponse);
    }
}