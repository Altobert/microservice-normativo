package cl.sii.normativo.loadnormas.controller;

import cl.sii.normativo.loadnormas.services.BulkIndexerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/bulk")
@Tag(name = "Indexación Masiva", description = "API para realizar indexación masiva de documentos")
public class BulkIndexController {
    
    @Autowired
    private BulkIndexerService bulkIndexerService;
    
    /**
     * Indexa todos los documentos PDF de un directorio específico
     */
    @Operation(
        summary = "Indexar directorio completo",
        description = "Indexa todos los documentos PDF encontrados en un directorio específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Indexación completada exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "message": "Indexación masiva completada",
                      "directory": "/ruta/al/directorio",
                      "successCount": 100,
                      "errorCount": 5,
                      "errors": ["archivo1.pdf: Error de lectura", "archivo2.pdf: PDF corrupto"],
                      "totalProcessed": 105
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Directorio no válido",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "error": "Directorio no válido",
                      "message": "El directorio especificado no existe o no es accesible"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error de indexación",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "error": "Error de indexación",
                      "message": "Error interno del servidor durante la indexación"
                    }
                    """)
            )
        )
    })
    @PostMapping("/index-directory")
    public ResponseEntity<Map<String, Object>> indexDirectory(
        @Parameter(description = "Ruta del directorio a indexar", required = true, example = "/Users/usuario/documentos")
        @RequestParam String directoryPath) {
        try {
            System.out.println("🚀 Iniciando indexación masiva del directorio: " + directoryPath);
            
            BulkIndexerService.BulkIndexResult result = bulkIndexerService.indexDirectory(directoryPath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Indexación masiva completada");
            response.put("directory", directoryPath);
            response.put("successCount", result.getSuccessCount());
            response.put("errorCount", result.getErrorCount());
            response.put("errors", result.getErrors());
            response.put("totalProcessed", result.getSuccessCount() + result.getErrorCount());
            
            System.out.println("✅ " + result.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Directorio no válido");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error de indexación");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Indexa documentos del directorio por defecto del SII
     */
    @PostMapping("/index-sii-documents")
    public ResponseEntity<Map<String, Object>> indexSIIDocuments() {
        String defaultPath = "/Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII";
        return indexDirectory(defaultPath);
    }
    
    /**
     * Indexa documentos de un año específico
     */
    @PostMapping("/index-year/{year}")
    public ResponseEntity<Map<String, Object>> indexYear(@PathVariable String year) {
        String yearPath = "/Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII/" + year;
        return indexDirectory(yearPath);
    }
    
    /**
     * Obtiene estadísticas del índice
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getIndexStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "Estadísticas del índice");
        stats.put("indexDirectory", "path/to/index");
        stats.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(stats);
    }
}
