# 🔌 Documentación de la API - Microservicio Normativo SII

Esta documentación describe todos los endpoints disponibles en el Microservicio Normativo SII para la búsqueda y gestión de documentos normativos.

## 📋 **Índice**

- [Información General](#información-general)
- [Endpoints de Búsqueda](#endpoints-de-búsqueda)
- [Endpoints de Gestión de Documentos](#endpoints-de-gestión-de-documentos)
- [Endpoints de Indexación Masiva](#endpoints-de-indexación-masiva)
- [Endpoints del Sistema](#endpoints-del-sistema)
- [Esquemas de Datos](#esquemas-de-datos)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Códigos de Error](#códigos-de-error)

## 🌐 **Información General**

- **Base URL**: `http://localhost:8080`
- **Versión de API**: 1.0.0
- **Formato**: JSON
- **Autenticación**: No requerida (desarrollo)

### **Swagger UI**
- **URL**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/api-docs`

---

## 🔍 **Endpoints de Búsqueda**

### **1. Buscar Documentos**
```http
GET /api/search/documents?query={term}&limit={number}&field={field}
```

**Parámetros:**
- `query` (requerido): Término de búsqueda
- `limit` (opcional): Número máximo de resultados (default: 10, max: 100)
- `field` (opcional): Campo a buscar (content, filename, title, documentId, year)

**Ejemplo:**
```bash
curl "http://localhost:8080/api/search/documents?query=IVA&limit=5&field=content"
```

**Respuesta:**
```json
{
  "query": "IVA",
  "field": "content",
  "limit": 5,
  "totalResults": 2,
  "results": [
    {
      "score": 1.2345955,
      "filename": "ID1302_Tributacion_Regimen_ADM_IVA_Servicios_Extranjeros.pdf",
      "title": "Tributación Régimen ADM IVA Servicios Extranjeros",
      "filepath": "/ruta/al/archivo.pdf",
      "year": "2020",
      "documentId": "ID1302",
      "size": "563916",
      "lastModified": "1728443774000",
      "snippet": "INSTRUCCIONES SOBRE TRIBUTACIÓN EN EL RÉGIMEN..."
    }
  ]
}
```

### **2. Buscar por Año**
```http
GET /api/search/documents/year/{year}?query={term}&limit={number}
```

**Parámetros:**
- `year` (path): Año en formato YYYY
- `query` (query): Término de búsqueda
- `limit` (query): Número máximo de resultados

**Ejemplo:**
```bash
curl "http://localhost:8080/api/search/documents/year/2020?query=impuesto&limit=10"
```

### **3. Buscar por ID**
```http
GET /api/search/documents/id/{documentId}
```

**Parámetros:**
- `documentId` (path): ID del documento (formato: ID####)

**Ejemplo:**
```bash
curl "http://localhost:8080/api/search/documents/id/ID1302"
```

### **4. Estadísticas del Índice**
```http
GET /api/search/stats
```

**Respuesta:**
```json
{
  "totalDocuments": 104,
  "indexDirectory": "path/to/index",
  "timestamp": 1757910350026,
  "documentsByYear": {
    "2019": 44,
    "2020": 308,
    "2021": 242
  }
}
```

---

## 📄 **Endpoints de Gestión de Documentos**

### **1. Subir Documento PDF**
```http
POST /api/documents/upload
Content-Type: multipart/form-data
```

**Parámetros:**
- `file` (form-data): Archivo PDF a indexar

**Ejemplo:**
```bash
curl -X POST "http://localhost:8080/api/documents/upload" \
  -F "file=@documento.pdf"
```

**Respuesta:**
```
Documento indexado exitosamente.
```

---

## 📚 **Endpoints de Indexación Masiva**

### **1. Indexar Directorio**
```http
POST /api/bulk/index-directory
Content-Type: application/x-www-form-urlencoded
```

**Parámetros:**
- `directoryPath` (form-data): Ruta del directorio a indexar

**Ejemplo:**
```bash
curl -X POST "http://localhost:8080/api/bulk/index-directory" \
  -d "directoryPath=/Users/usuario/documentos"
```

**Respuesta:**
```json
{
  "message": "Indexación masiva completada",
  "directory": "/Users/usuario/documentos",
  "successCount": 100,
  "errorCount": 5,
  "errors": ["archivo1.pdf: Error de lectura"],
  "totalProcessed": 105
}
```

### **2. Indexar Documentos SII**
```http
POST /api/bulk/index-sii-documents
```

Indexa el directorio por defecto del SII.

### **3. Indexar por Año**
```http
POST /api/bulk/index-year/{year}
```

**Parámetros:**
- `year` (path): Año en formato YYYY

**Ejemplo:**
```bash
curl -X POST "http://localhost:8080/api/bulk/index-year/2020"
```

### **4. Estadísticas de Indexación**
```http
GET /api/bulk/stats
```

---

## ⚙️ **Endpoints del Sistema**

### **1. Health Check**
```http
GET /actuator/health
```

**Respuesta:**
```json
{
  "status": "UP"
}
```

---

## 📊 **Esquemas de Datos**

### **DocumentResult**
```json
{
  "score": 1.2345955,
  "filename": "documento.pdf",
  "title": "Título del documento",
  "filepath": "/ruta/al/archivo.pdf",
  "year": "2020",
  "documentId": "ID1302",
  "size": "563916",
  "lastModified": "1728443774000",
  "snippet": "Fragmento del contenido..."
}
```

### **ErrorResponse**
```json
{
  "error": "Tipo de error",
  "message": "Mensaje de error detallado"
}
```

### **BulkIndexResponse**
```json
{
  "message": "Indexación masiva completada",
  "directory": "/ruta/directorio",
  "successCount": 100,
  "errorCount": 5,
  "errors": ["error1", "error2"],
  "totalProcessed": 105
}
```

---

## 💡 **Ejemplos de Uso**

### **Búsqueda Básica**
```bash
# Buscar documentos sobre IVA
curl "http://localhost:8080/api/search/documents?query=IVA"

# Buscar en títulos
curl "http://localhost:8080/api/search/documents?query=impuesto&field=title"

# Limitar resultados
curl "http://localhost:8080/api/search/documents?query=tributacion&limit=5"
```

### **Búsqueda Avanzada**
```bash
# Buscar documentos de 2020 sobre impuestos
curl "http://localhost:8080/api/search/documents/year/2020?query=impuesto"

# Buscar documento específico
curl "http://localhost:8080/api/search/documents/id/ID1302"
```

### **Indexación**
```bash
# Indexar directorio específico
curl -X POST "http://localhost:8080/api/bulk/index-directory" \
  -d "directoryPath=/Users/usuario/documentos"

# Indexar documentos SII por defecto
curl -X POST "http://localhost:8080/api/bulk/index-sii-documents"

# Indexar año específico
curl -X POST "http://localhost:8080/api/bulk/index-year/2020"
```

### **Monitoreo**
```bash
# Verificar salud del servicio
curl "http://localhost:8080/actuator/health"

# Obtener estadísticas
curl "http://localhost:8080/api/search/stats"
```

---

## ❌ **Códigos de Error**

| Código | Descripción | Causa Común |
|--------|-------------|-------------|
| 400 | Bad Request | Parámetros inválidos o directorio no existe |
| 500 | Internal Server Error | Error en el servidor o índice corrupto |

### **Ejemplos de Errores**

**Error 400:**
```json
{
  "error": "Directorio no válido",
  "message": "El directorio especificado no existe o no es accesible"
}
```

**Error 500:**
```json
{
  "error": "Error en la búsqueda",
  "message": "Error interno del servidor durante la búsqueda"
}
```

---

## 🔧 **Configuración**

### **Variables de Entorno**
- `lucene.index.directory`: Directorio del índice Lucene (default: "path/to/index")
- `server.port`: Puerto del servidor (default: 8080)

### **Límites**
- Máximo 100 resultados por búsqueda
- Archivos PDF hasta 50MB
- Timeout de indexación: 30 minutos

---

## 📝 **Notas Importantes**

1. **Indexación**: Los documentos deben ser indexados antes de poder buscarlos
2. **Formato de IDs**: Los IDs de documentos siguen el formato `ID####`
3. **Campos de Búsqueda**: Los campos disponibles son: content, filename, title, documentId, year
4. **Performance**: Las búsquedas son más rápidas en campos indexados específicos
5. **Snippets**: Los snippets se limitan a 200 caracteres para optimizar la respuesta

---

*Para más detalles técnicos, consulta el archivo [openapi.json](openapi.json)*
