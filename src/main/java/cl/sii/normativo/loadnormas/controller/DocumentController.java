package cl.sii.normativo.loadnormas.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Gestión de Documentos", description = "API para la gestión de documentos normativos")
public class DocumentController {

    @Operation(
        summary = "Información del servicio",
        description = "Retorna información sobre el servicio de búsqueda de documentos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Información del servicio obtenida exitosamente",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                    {
                      "service": "Microservicio de Búsqueda de Documentos Normativos SII",
                      "version": "1.0",
                      "description": "Servicio especializado en búsquedas de texto completo en índices Lucene",
                      "capabilities": [
                        "Búsqueda de texto completo",
                        "Búsqueda por año",
                        "Búsqueda por ID de documento",
                        "Estadísticas del índice"
                      ],
                      "timestamp": 1757910350026
                    }
                    """)
            )
        )
    })
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        System.out.println("Solicitando información del servicio de búsqueda");
        
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Microservicio de Búsqueda de Documentos Normativos SII");
        info.put("version", "1.0");
        info.put("description", "Servicio especializado en búsquedas de texto completo en índices Lucene");
        info.put("capabilities", new String[]{
            "Búsqueda de texto completo",
            "Búsqueda por año", 
            "Búsqueda por ID de documento",
            "Estadísticas del índice"
        });
        info.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(info);
    }

    @Operation(
        summary = "Estado del servicio",
        description = "Retorna el estado actual del servicio de búsqueda."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Estado del servicio obtenido exitosamente",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                    {
                      "status": "UP",
                      "service": "search-service",
                      "timestamp": 1757910350026
                    }
                    """)
            )
        )
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        System.out.println("Solicitando estado del servicio de búsqueda");
        
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "search-service");
        status.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(status);
    }
}