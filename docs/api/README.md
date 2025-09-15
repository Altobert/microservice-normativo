# 🔌 Documentación de la API - Microservicio Normativo SII

Esta carpeta contiene toda la documentación relacionada con la API del Microservicio Normativo SII.

## 📋 **Archivos Disponibles**

### 📖 **API_DOCUMENTATION.md**
Documentación completa de todos los endpoints disponibles:
- Endpoints de búsqueda
- Endpoints de gestión de documentos
- Endpoints de indexación masiva
- Endpoints del sistema
- Esquemas de datos
- Códigos de error

### 🚀 **API_EXAMPLES.md**
Ejemplos prácticos y casos de uso reales:
- Casos de uso comunes
- Ejemplos de búsqueda
- Ejemplos de indexación
- Scripts de automatización
- Integración con aplicaciones (Python, JavaScript, Java)
- Troubleshooting

### 📄 **openapi.json**
Especificación completa de OpenAPI 3.0:
- Definición completa de todos los endpoints
- Esquemas de datos detallados
- Ejemplos de request/response
- Documentación técnica para desarrolladores

### 📚 **DOCUMENTACION_SWAGGER.md**
Documentación específica de Swagger UI:
- Configuración de Swagger
- Uso de la interfaz web
- Personalización de la documentación

## 🚀 **Acceso Rápido**

### **Swagger UI Interactivo**
```
http://localhost:8080/swagger-ui.html
```

### **Especificación OpenAPI**
```
http://localhost:8080/api-docs
```

### **Health Check**
```
http://localhost:8080/actuator/health
```

## 📊 **Endpoints Principales**

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/search/documents` | GET | Buscar documentos |
| `/api/search/documents/year/{year}` | GET | Buscar por año |
| `/api/search/documents/id/{id}` | GET | Buscar por ID |
| `/api/search/stats` | GET | Estadísticas del índice |
| `/api/documents/upload` | POST | Subir documento PDF |
| `/api/bulk/index-directory` | POST | Indexar directorio |
| `/api/bulk/index-sii-documents` | POST | Indexar documentos SII |
| `/api/bulk/index-year/{year}` | POST | Indexar por año |
| `/actuator/health` | GET | Health check |

## 💡 **Ejemplos Rápidos**

### **Búsqueda Básica**
```bash
curl "http://localhost:8080/api/search/documents?query=IVA&limit=10"
```

### **Búsqueda por Año**
```bash
curl "http://localhost:8080/api/search/documents/year/2020?query=impuesto&limit=10"
```

### **Subir Documento**
```bash
curl -X POST "http://localhost:8080/api/documents/upload" \
  -F "file=@documento.pdf"
```

### **Indexar Directorio**
```bash
curl -X POST "http://localhost:8080/api/bulk/index-directory" \
  -d "directoryPath=/ruta/directorio"
```

## 🔧 **Configuración**

### **Variables de Entorno**
- `lucene.index.directory`: Directorio del índice Lucene
- `server.port`: Puerto del servidor (default: 8080)

### **Límites de la API**
- Máximo 100 resultados por búsqueda
- Archivos PDF hasta 50MB
- Timeout de indexación: 30 minutos

## 📝 **Notas Importantes**

1. **Indexación**: Los documentos deben ser indexados antes de poder buscarlos
2. **Formato de IDs**: Los IDs siguen el formato `ID####`
3. **Campos de Búsqueda**: content, filename, title, documentId, year
4. **Performance**: Las búsquedas son más rápidas en campos específicos
5. **Snippets**: Limitados a 200 caracteres para optimizar respuestas

## 🔗 **Enlaces Útiles**

- [Documentación Completa](API_DOCUMENTATION.md)
- [Ejemplos Prácticos](API_EXAMPLES.md)
- [Especificación OpenAPI](openapi.json)
- [Swagger UI](http://localhost:8080/swagger-ui.html)

---

*Para más detalles técnicos, consulta los archivos específicos en esta carpeta.*
