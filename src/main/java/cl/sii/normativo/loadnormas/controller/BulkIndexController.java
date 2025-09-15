package cl.sii.normativo.loadnormas.controller;

import cl.sii.normativo.loadnormas.services.BulkIndexerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bulk")
public class BulkIndexController {
    
    @Autowired
    private BulkIndexerService bulkIndexerService;
    
    /**
     * Indexa todos los documentos PDF de un directorio específico
     */
    @PostMapping("/index-directory")
    public ResponseEntity<Map<String, Object>> indexDirectory(@RequestParam String directoryPath) {
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
