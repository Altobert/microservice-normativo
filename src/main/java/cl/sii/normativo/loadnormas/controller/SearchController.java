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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.sii.normativo.loadnormas.services.HybridSearchService;
import cl.sii.normativo.loadnormas.services.VectorIndexerService;

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
public class SearchController {
    
    @Value("${lucene.index.directory:path/to/index}")
    private String indexDir;
    
    @Autowired
    private HybridSearchService hybridSearchService;
    
    @Autowired
    private VectorIndexerService vectorIndexerService;
    
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
        
        System.out.println("Iniciando búsqueda de documentos - Query: '" + query + "', Campo: '" + field + "', Límite: " + limit);
        
        try {
            List<Map<String, Object>> results = performSearch(query, limit, field);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("field", field);
            response.put("limit", limit);
            response.put("totalResults", results.size());
            response.put("results", results);
            
            System.out.println("Búsqueda completada exitosamente - " + results.size() + " resultados encontrados para query: '" + query + "'");
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            System.out.println("Error durante la búsqueda de documentos - Query: '" + query + "', Campo: '" + field + "', Límite: " + limit + " - " + e.getMessage());
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
        
        System.out.println("Iniciando búsqueda por año - Query: '" + query + "', Año: '" + year + "', Límite: " + limit);
        
        try {
            String yearQuery = query + " AND year:" + year;
            List<Map<String, Object>> results = performSearch(yearQuery, limit, "content");
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("year", year);
            response.put("limit", limit);
            response.put("totalResults", results.size());
            response.put("results", results);
            
            System.out.println("Búsqueda por año completada - " + results.size() + " resultados encontrados para query: '" + query + "' en año: '" + year + "'");
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            System.out.println("Error durante la búsqueda por año - Query: '" + query + "', Año: '" + year + "', Límite: " + limit + " - " + e.getMessage());
            return createErrorResponse("Error en la búsqueda por año", e.getMessage());
        }
    }
    
    /**
     * Busca documentos por ID específico
     */
    @GetMapping("/documents/id/{documentId}")
    public ResponseEntity<Map<String, Object>> searchDocumentById(@PathVariable String documentId) {
        
        System.out.println("Iniciando búsqueda por ID de documento - DocumentId: '" + documentId + "'");
        
        try {
            String query = "documentId:" + documentId;
            List<Map<String, Object>> results = performSearch(query, 1, "documentId");
            
            Map<String, Object> response = new HashMap<>();
            response.put("documentId", documentId);
            response.put("totalResults", results.size());
            response.put("results", results);
            
            System.out.println("Búsqueda por ID completada - " + results.size() + " resultados encontrados para DocumentId: '" + documentId + "'");
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            System.out.println("Error durante la búsqueda por ID - DocumentId: '" + documentId + "' - " + e.getMessage());
            return createErrorResponse("Error en la búsqueda por ID", e.getMessage());
        }
    }
    
    /**
     * Realiza la búsqueda en el índice de Lucene
     */
    private List<Map<String, Object>> performSearch(String queryString, int limit, String field) 
            throws IOException, ParseException {
        
        System.out.println("Ejecutando búsqueda en Lucene - Query: '" + queryString + "', Campo: '" + field + "', Límite: " + limit);
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             DirectoryReader reader = DirectoryReader.open(dir)) {
            
            System.out.println("Directorio de índice abierto: " + indexDir);
            System.out.println("Número total de documentos en el índice: " + reader.numDocs());
            
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser(field, new StandardAnalyzer());
            Query query = parser.parse(queryString);
            
            TopDocs topDocs = searcher.search(query, limit);
            System.out.println("Búsqueda ejecutada - " + topDocs.totalHits.value + " documentos encontrados");
            
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
                System.out.println("Documento procesado - ID: " + doc.get("documentId") + ", Score: " + scoreDoc.score + ", Filename: " + doc.get("filename"));
            }
        }
        
        System.out.println("Búsqueda completada - " + results.size() + " resultados procesados");
        return results;
    }
    
    /**
     * Búsqueda híbrida que combina búsqueda tradicional y vectorial
     */
    @Operation(
        summary = "Búsqueda híbrida",
        description = """
            Realiza una búsqueda que combina búsqueda tradicional por términos exactos y búsqueda vectorial por similitud semántica.
            
            **Casos de uso recomendados:**
            - Consultas específicas: traditionalWeight=0.7, vectorWeight=0.3
            - Consultas semánticas: traditionalWeight=0.3, vectorWeight=0.7
            - Consultas balanceadas: traditionalWeight=0.5, vectorWeight=0.5
            
            **Tipos de match:**
            - `traditional`: Solo encontrado en búsqueda tradicional
            - `vector`: Solo encontrado en búsqueda vectorial
            - `hybrid`: Encontrado en ambas búsquedas
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Búsqueda híbrida realizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Consulta específica (más peso tradicional)",
                        summary = "Búsqueda con más peso en términos exactos",
                        value = """
                            {
                              "query": "IVA",
                              "limit": 5,
                              "traditionalWeight": 0.7,
                              "vectorWeight": 0.3,
                              "totalResults": 2,
                              "results": [
                                {
                                  "documentId": "ID1302",
                                  "filename": "Circular_IVA_2024.pdf",
                                  "title": "Circular sobre IVA",
                                  "year": "2024",
                                  "traditionalScore": 2.5,
                                  "vectorScore": 0.8,
                                  "hybridScore": 1.99,
                                  "matchType": "hybrid",
                                  "snippet": "El Impuesto al Valor Agregado (IVA) se aplica sobre..."
                                }
                              ]
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Consulta semántica (más peso vectorial)",
                        summary = "Búsqueda con más peso en similitud semántica",
                        value = """
                            {
                              "query": "impuestos sobre la renta de personas naturales",
                              "limit": 10,
                              "traditionalWeight": 0.3,
                              "vectorWeight": 0.7,
                              "totalResults": 4,
                              "results": [
                                {
                                  "documentId": "ID1501",
                                  "filename": "Ley_Renta_Personas.pdf",
                                  "title": "Ley de Impuesto a la Renta",
                                  "year": "2023",
                                  "traditionalScore": 0.5,
                                  "vectorScore": 1.8,
                                  "hybridScore": 1.41,
                                  "matchType": "hybrid",
                                  "snippet": "El impuesto a la renta de las personas naturales se determina..."
                                }
                              ]
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Consulta balanceada",
                        summary = "Búsqueda con pesos equilibrados",
                        value = """
                            {
                              "query": "obligaciones tributarias",
                              "limit": 8,
                              "traditionalWeight": 0.5,
                              "vectorWeight": 0.5,
                              "totalResults": 3,
                              "results": [
                                {
                                  "documentId": "ID2001",
                                  "filename": "Obligaciones_Tributarias.pdf",
                                  "title": "Guía de Obligaciones Tributarias",
                                  "year": "2024",
                                  "traditionalScore": 1.2,
                                  "vectorScore": 1.1,
                                  "hybridScore": 1.15,
                                  "matchType": "hybrid",
                                  "snippet": "Las obligaciones tributarias incluyen la declaración y pago..."
                                }
                              ]
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error en la búsqueda híbrida",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "error": "Error en la búsqueda híbrida",
                      "message": "Mensaje de error específico"
                    }
                    """)
            )
        )
    })
    @GetMapping("/hybrid")
    public ResponseEntity<Map<String, Object>> hybridSearch(
            @Parameter(description = "Consulta de búsqueda", required = true, example = "impuestos sobre la renta")
            @RequestParam String query,
            @Parameter(description = "Número máximo de resultados", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Peso para búsqueda tradicional (0.0-1.0)", example = "0.6")
            @RequestParam(defaultValue = "0.6") double traditionalWeight,
            @Parameter(description = "Peso para búsqueda vectorial (0.0-1.0)", example = "0.4")
            @RequestParam(defaultValue = "0.4") double vectorWeight) {
        
        System.out.println("Iniciando búsqueda híbrida - Query: '" + query + "', Límite: " + limit + 
                         ", Pesos - Tradicional: " + traditionalWeight + ", Vectorial: " + vectorWeight);
        
        try {
            List<HybridSearchService.HybridSearchResult> results = hybridSearchService.hybridSearch(
                query, limit, traditionalWeight, vectorWeight);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("limit", limit);
            response.put("traditionalWeight", traditionalWeight);
            response.put("vectorWeight", vectorWeight);
            response.put("totalResults", results.size());
            
            // Convertir resultados a formato de respuesta
            List<Map<String, Object>> formattedResults = new ArrayList<>();
            for (HybridSearchService.HybridSearchResult result : results) {
                Map<String, Object> formattedResult = new HashMap<>();
                formattedResult.put("documentId", result.getDocumentId());
                formattedResult.put("filename", result.getFilename());
                formattedResult.put("title", result.getTitle());
                formattedResult.put("year", result.getYear());
                formattedResult.put("traditionalScore", result.getTraditionalScore());
                formattedResult.put("vectorScore", result.getVectorScore());
                formattedResult.put("hybridScore", result.getHybridScore());
                formattedResult.put("matchType", result.getMatchType());
                
                // Mostrar snippet del contenido
                String content = result.getContent();
                if (content != null && content.length() > 200) {
                    formattedResult.put("snippet", content.substring(0, 200) + "...");
                } else {
                    formattedResult.put("snippet", content);
                }
                
                formattedResults.add(formattedResult);
            }
            
            response.put("results", formattedResults);
            
            System.out.println("Búsqueda híbrida completada exitosamente - " + results.size() + 
                             " resultados encontrados para query: '" + query + "'");
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            System.out.println("Error durante la búsqueda híbrida - Query: '" + query + "' - " + e.getMessage());
            return createErrorResponse("Error en la búsqueda híbrida", e.getMessage());
        }
    }
    
    /**
     * Búsqueda híbrida inteligente con pesos automáticos
     */
    @Operation(
        summary = "Búsqueda híbrida inteligente",
        description = """
            Realiza una búsqueda híbrida con pesos automáticos basados en las características de la consulta.
            
            **Algoritmo de pesos automáticos:**
            - Consultas cortas (≤50 chars): traditionalWeight=0.6, vectorWeight=0.4
            - Consultas largas (>50 chars): traditionalWeight=0.4, vectorWeight=0.6
            - Palabras muy cortas (≤3 chars): traditionalWeight=0.7, vectorWeight=0.3
            
            **Ventajas:**
            - No requiere configuración manual de pesos
            - Se adapta automáticamente al tipo de consulta
            - Optimiza resultados según el contexto
            """
    )
    @GetMapping("/smart-hybrid")
    public ResponseEntity<Map<String, Object>> smartHybridSearch(
            @Parameter(description = "Consulta de búsqueda", required = true, example = "impuestos sobre la renta")
            @RequestParam String query,
            @Parameter(description = "Número máximo de resultados", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        
        System.out.println("Iniciando búsqueda híbrida inteligente - Query: '" + query + "', Límite: " + limit);
        
        try {
            List<HybridSearchService.HybridSearchResult> results = hybridSearchService.smartHybridSearch(query, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("limit", limit);
            response.put("searchType", "smart-hybrid");
            response.put("totalResults", results.size());
            
            // Convertir resultados a formato de respuesta
            List<Map<String, Object>> formattedResults = new ArrayList<>();
            for (HybridSearchService.HybridSearchResult result : results) {
                Map<String, Object> formattedResult = new HashMap<>();
                formattedResult.put("documentId", result.getDocumentId());
                formattedResult.put("filename", result.getFilename());
                formattedResult.put("title", result.getTitle());
                formattedResult.put("year", result.getYear());
                formattedResult.put("traditionalScore", result.getTraditionalScore());
                formattedResult.put("vectorScore", result.getVectorScore());
                formattedResult.put("hybridScore", result.getHybridScore());
                formattedResult.put("matchType", result.getMatchType());
                
                // Mostrar snippet del contenido
                String content = result.getContent();
                if (content != null && content.length() > 200) {
                    formattedResult.put("snippet", content.substring(0, 200) + "...");
                } else {
                    formattedResult.put("snippet", content);
                }
                
                formattedResults.add(formattedResult);
            }
            
            response.put("results", formattedResults);
            
            System.out.println("Búsqueda híbrida inteligente completada - " + results.size() + 
                             " resultados encontrados para query: '" + query + "'");
            return ResponseEntity.ok(response);
            
        } catch (IOException | ParseException e) {
            System.out.println("Error durante la búsqueda híbrida inteligente - Query: '" + query + "' - " + e.getMessage());
            return createErrorResponse("Error en la búsqueda híbrida inteligente", e.getMessage());
        }
    }
    
    /**
     * Obtiene estadísticas del sistema de búsqueda híbrida
     */
    @Operation(
        summary = "Estadísticas del sistema híbrido",
        description = """
            Retorna información detallada sobre el estado del sistema de búsqueda híbrida.
            
            **Información incluida:**
            - Estado del servicio de embeddings
            - Tamaño del cache de embeddings
            - Estadísticas del índice vectorial
            - Configuración del directorio de índice
            - Campos soportados para búsqueda
            
            **Útil para:**
            - Monitoreo del sistema
            - Diagnóstico de problemas
            - Optimización de rendimiento
            """
    )
    @GetMapping("/hybrid-stats")
    public ResponseEntity<Map<String, Object>> getHybridStats() {
        System.out.println("Solicitando estadísticas del sistema híbrido");
        
        try {
            Map<String, Object> hybridStats = hybridSearchService.getSearchStats();
            Map<String, Object> vectorStats = vectorIndexerService.getIndexStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("hybridSearch", hybridStats);
            response.put("vectorIndexer", vectorStats);
            response.put("timestamp", System.currentTimeMillis());
            
            System.out.println("Estadísticas del sistema híbrido generadas exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("Error al obtener estadísticas del sistema híbrido - " + e.getMessage());
            return createErrorResponse("Error al obtener estadísticas del sistema híbrido", e.getMessage());
        }
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
        System.out.println("Solicitando estadísticas del índice");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            try (Directory dir = FSDirectory.open(Paths.get(indexDir));
                 DirectoryReader reader = DirectoryReader.open(dir)) {
                
                int totalDocs = reader.numDocs();
                System.out.println("Procesando estadísticas - Total documentos: " + totalDocs);
                
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
                
                System.out.println("Estadísticas generadas exitosamente - " + totalDocs + " documentos totales, " + yearCount.size() + " años diferentes");
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (IOException e) {
            System.out.println("Error al obtener estadísticas del índice - " + e.getMessage());
            return createErrorResponse("Error al obtener estadísticas", e.getMessage());
        }
    }
    
    /**
     * Crea una respuesta de error estandarizada
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String errorType, String message) {
        System.out.println("Creando respuesta de error - Tipo: '" + errorType + "', Mensaje: '" + message + "'");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorType);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(500).body(errorResponse);
    }
}