package cl.sii.normativo.loadnormas.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @InjectMocks
    private DocumentController documentController;

    @Test
    void testGetServiceInfo() {
        // Act
        ResponseEntity<Map<String, Object>> response = documentController.getServiceInfo();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertEquals("Microservicio de Búsqueda de Documentos Normativos SII", body.get("service"));
        assertEquals("1.0", body.get("version"));
        assertEquals("Servicio especializado en búsquedas de texto completo en índices Lucene", body.get("description"));
        assertNotNull(body.get("capabilities"));
        assertNotNull(body.get("timestamp"));
        
        // Verificar que capabilities es un array con los elementos esperados
        String[] capabilities = (String[]) body.get("capabilities");
        assertEquals(4, capabilities.length);
        assertTrue(java.util.Arrays.asList(capabilities).contains("Búsqueda de texto completo"));
        assertTrue(java.util.Arrays.asList(capabilities).contains("Búsqueda por año"));
        assertTrue(java.util.Arrays.asList(capabilities).contains("Búsqueda por ID de documento"));
        assertTrue(java.util.Arrays.asList(capabilities).contains("Estadísticas del índice"));
    }

    @Test
    void testGetServiceStatus() {
        // Act
        ResponseEntity<Map<String, Object>> response = documentController.getServiceStatus();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertEquals("UP", body.get("status"));
        assertEquals("search-service", body.get("service"));
        assertNotNull(body.get("timestamp"));
        
        // Verificar que el timestamp es un número válido
        assertTrue(body.get("timestamp") instanceof Number);
        assertTrue(((Number) body.get("timestamp")).longValue() > 0);
    }

    @Test
    void testServiceInfoResponseStructure() {
        // Act
        ResponseEntity<Map<String, Object>> response = documentController.getServiceInfo();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        
        // Verificar que todos los campos requeridos están presentes
        assertTrue(body.containsKey("service"));
        assertTrue(body.containsKey("version"));
        assertTrue(body.containsKey("description"));
        assertTrue(body.containsKey("capabilities"));
        assertTrue(body.containsKey("timestamp"));
        
        // Verificar que no hay campos nulos
        assertNotNull(body.get("service"));
        assertNotNull(body.get("version"));
        assertNotNull(body.get("description"));
        assertNotNull(body.get("capabilities"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void testServiceStatusResponseStructure() {
        // Act
        ResponseEntity<Map<String, Object>> response = documentController.getServiceStatus();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        
        // Verificar que todos los campos requeridos están presentes
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("service"));
        assertTrue(body.containsKey("timestamp"));
        
        // Verificar que no hay campos nulos
        assertNotNull(body.get("status"));
        assertNotNull(body.get("service"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void testServiceInfoContentValidation() {
        // Act
        ResponseEntity<Map<String, Object>> response = documentController.getServiceInfo();

        // Assert
        Map<String, Object> body = response.getBody();
        
        // Verificar contenido específico
        String service = (String) body.get("service");
        assertTrue(service.contains("Microservicio de Búsqueda"));
        assertTrue(service.contains("Documentos Normativos SII"));
        
        String description = (String) body.get("description");
        assertTrue(description.contains("búsquedas de texto completo"));
        assertTrue(description.contains("índices Lucene"));
        
        // Verificar que el timestamp es reciente (dentro de los últimos 5 segundos)
        long timestamp = ((Number) body.get("timestamp")).longValue();
        long currentTime = System.currentTimeMillis();
        assertTrue(Math.abs(currentTime - timestamp) < 5000);
    }

    @Test
    void testServiceStatusContentValidation() {
        // Act
        ResponseEntity<Map<String, Object>> response = documentController.getServiceStatus();

        // Assert
        Map<String, Object> body = response.getBody();
        
        // Verificar contenido específico
        assertEquals("UP", body.get("status"));
        assertEquals("search-service", body.get("service"));
        
        // Verificar que el timestamp es reciente (dentro de los últimos 5 segundos)
        long timestamp = ((Number) body.get("timestamp")).longValue();
        long currentTime = System.currentTimeMillis();
        assertTrue(Math.abs(currentTime - timestamp) < 5000);
    }
}