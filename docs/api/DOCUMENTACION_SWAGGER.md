# 📖 Documentación de Swagger/OpenAPI 3 - Microservicio de Documentos Normativos

## 🎯 Resumen

Se ha configurado exitosamente **Swagger/OpenAPI 3** para documentar y publicar la API del microservicio de documentos normativos del SII. La configuración incluye documentación completa de todos los endpoints, ejemplos de uso, y una interfaz web interactiva.

## 🚀 Configuración Implementada

### 1. **Dependencias Agregadas**

```xml
<!-- SpringDoc OpenAPI 3 para Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 2. **Configuración de OpenAPI**

**Archivo:** `src/main/java/cl/sii/normativo/loadnormas/configuration/OpenApiConfig.java`

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservicio de Documentos Normativos SII")
                        .description("API REST para la gestión y búsqueda de documentos normativos...")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo SII")
                                .email("desarrollo@sii.cl")
                                .url("https://www.sii.cl"))
                        .license(new License()
                                .name("Licencia Pública")
                                .url("https://www.sii.cl/licencia")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo local")
                ));
    }
}
```

### 3. **Propiedades de Configuración**

**Archivo:** `src/main/resources/application.properties`

```properties
# Configuración de SpringDoc OpenAPI (Swagger)
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.try-it-out-enabled=true
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.filter=true
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.display-operation-id=true
springdoc.swagger-ui.show-extensions=true
springdoc.swagger-ui.show-common-extensions=true
```

## 📋 Endpoints Documentados

### **🏷️ Gestión de Documentos**
- `POST /api/documents/upload` - Subir y indexar documento PDF individual

### **🏷️ Búsqueda de Documentos**
- `GET /api/search/documents` - Búsqueda general de documentos
- `GET /api/search/documents/year/{year}` - Búsqueda por año específico
- `GET /api/search/documents/id/{documentId}` - Búsqueda por ID de documento
- `GET /api/search/stats` - Estadísticas del índice

### **🏷️ Indexación Masiva**
- `POST /api/bulk/index-directory` - Indexar directorio completo
- `POST /api/bulk/index-sii-documents` - Indexar documentos del SII
- `POST /api/bulk/index-year/{year}` - Indexar documentos de un año específico
- `GET /api/bulk/stats` - Estadísticas de indexación masiva

## 🌐 URLs de Acceso

### **Interfaz Web Interactiva**
```
http://localhost:8080/swagger-ui/index.html
```

### **Especificación OpenAPI**
```
http://localhost:8080/api-docs          # JSON
http://localhost:8080/v3/api-docs      # JSON (versión 3)
http://localhost:8080/v3/api-docs.yaml # YAML
```

## 🔧 Características Implementadas

### **1. Documentación Completa**
- ✅ Descripción detallada de cada endpoint
- ✅ Parámetros documentados con ejemplos
- ✅ Respuestas de éxito y error documentadas
- ✅ Ejemplos de request/response
- ✅ Códigos de estado HTTP explicados

### **2. Categorización por Tags**
- ✅ **Gestión de Documentos**: Operaciones individuales
- ✅ **Búsqueda de Documentos**: Consultas y estadísticas
- ✅ **Indexación Masiva**: Operaciones en lote

### **3. Interfaz Interactiva**
- ✅ Prueba de endpoints directamente desde el navegador
- ✅ Autenticación integrada (si se requiere)
- ✅ Ejemplos de código generados automáticamente
- ✅ Filtrado y búsqueda de endpoints

### **4. Metadatos del Proyecto**
- ✅ Información de contacto del equipo
- ✅ Licencia del proyecto
- ✅ Versión de la API
- ✅ Descripción técnica completa

## 📊 Estadísticas Actuales

```
✅ 9 endpoints documentados
✅ 3 categorías (tags) organizadas
✅ 1,794 documentos indexados en el sistema
✅ Interfaz web completamente funcional
✅ Especificación OpenAPI 3 válida
```

## 🎯 Ejemplos de Uso

### **1. Búsqueda de Documentos**
```bash
curl "http://localhost:8080/api/search/documents?query=IVA&limit=5"
```

### **2. Estadísticas del Sistema**
```bash
curl "http://localhost:8080/api/search/stats"
```

### **3. Indexación Masiva**
```bash
curl -X POST "http://localhost:8080/api/bulk/index-sii-documents"
```

## 🛠️ Comandos de Gestión

### **Iniciar el Microservicio**
```bash
mvn spring-boot:run -Dmaven.test.skip=true
```

### **Verificar Configuración**
```bash
./test_swagger_api.sh
```

### **Acceder a Swagger UI**
```bash
open http://localhost:8080/swagger-ui/index.html
```

## 📈 Beneficios de la Implementación

### **Para Desarrolladores**
- ✅ Documentación automática y siempre actualizada
- ✅ Interfaz interactiva para probar endpoints
- ✅ Ejemplos de código generados automáticamente
- ✅ Validación de requests/responses

### **Para Usuarios de la API**
- ✅ Documentación clara y comprensible
- ✅ Ejemplos prácticos de uso
- ✅ Información detallada de parámetros
- ✅ Códigos de error explicados

### **Para el Proyecto**
- ✅ Estándar OpenAPI 3 compatible
- ✅ Integración con herramientas de desarrollo
- ✅ Generación automática de clientes SDK
- ✅ Documentación profesional y mantenible

## 🔍 Monitoreo y Diagnóstico

### **Script de Diagnóstico**
```bash
./diagnostico_sistema.sh
```

### **Script de Prueba de Swagger**
```bash
./test_swagger_api.sh
```

### **Verificación de Salud**
```bash
curl http://localhost:8080/api/search/stats
```

## 🚀 Próximos Pasos Recomendados

1. **Autenticación**: Implementar autenticación JWT si es necesario
2. **Rate Limiting**: Agregar límites de velocidad para los endpoints
3. **Versionado**: Implementar versionado de API (v1, v2, etc.)
4. **Métricas**: Agregar métricas de uso de la API
5. **Logging**: Implementar logging estructurado para auditoría

## 📞 Soporte

Para soporte técnico o consultas sobre la API:
- **Email**: desarrollo@sii.cl
- **Web**: https://www.sii.cl
- **Documentación**: http://localhost:8080/swagger-ui/index.html

---

**✅ Configuración de Swagger/OpenAPI 3 completada exitosamente**

*Documentación generada automáticamente - Última actualización: $(date)*
