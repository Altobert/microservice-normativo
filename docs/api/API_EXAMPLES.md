# 🚀 Ejemplos Prácticos de la API - Microservicio Normativo SII

Esta guía contiene ejemplos prácticos y casos de uso reales para la API del Microservicio Normativo SII.

## 📋 **Índice**

- [Casos de Uso Comunes](#casos-de-uso-comunes)
- [Ejemplos de Búsqueda](#ejemplos-de-búsqueda)
- [Ejemplos de Indexación](#ejemplos-de-indexación)
- [Scripts de Automatización](#scripts-de-automatización)
- [Integración con Aplicaciones](#integración-con-aplicaciones)

---

## 🎯 **Casos de Uso Comunes**

### **1. Búsqueda de Normativas por Tema**
```bash
# Buscar normativas sobre IVA
curl "http://localhost:8080/api/search/documents?query=IVA&limit=10"

# Buscar normativas sobre impuestos a la renta
curl "http://localhost:8080/api/search/documents?query=impuesto%20renta&limit=10"

# Buscar normativas sobre declaraciones
curl "http://localhost:8080/api/search/documents?query=declaracion&limit=10"
```

### **2. Búsqueda por Período**
```bash
# Buscar normativas de 2020
curl "http://localhost:8080/api/search/documents/year/2020?query=impuesto&limit=20"

# Buscar normativas de 2021 sobre IVA
curl "http://localhost:8080/api/search/documents/year/2021?query=IVA&limit=15"
```

### **3. Búsqueda de Documentos Específicos**
```bash
# Buscar documento por ID específico
curl "http://localhost:8080/api/search/documents/id/ID1302"

# Buscar por nombre de archivo
curl "http://localhost:8080/api/search/documents?query=ID1302&field=filename"
```

---

## 🔍 **Ejemplos de Búsqueda**

### **Búsquedas Básicas**

```bash
# 1. Búsqueda simple
curl "http://localhost:8080/api/search/documents?query=tributacion"

# 2. Búsqueda con límite
curl "http://localhost:8080/api/search/documents?query=impuesto&limit=5"

# 3. Búsqueda en campo específico
curl "http://localhost:8080/api/search/documents?query=2020&field=year"
```

### **Búsquedas Avanzadas**

```bash
# 1. Búsqueda por año específico
curl "http://localhost:8080/api/search/documents/year/2020?query=IVA&limit=10"

# 2. Búsqueda por ID de documento
curl "http://localhost:8080/api/search/documents/id/ID1302"

# 3. Búsqueda en títulos
curl "http://localhost:8080/api/search/documents?query=circular&field=title&limit=20"
```

### **Búsquedas Específicas por Contenido**

```bash
# Buscar normativas sobre retiros
curl "http://localhost:8080/api/search/documents?query=retiro&limit=10"

# Buscar normativas sobre fondos de pensiones
curl "http://localhost:8080/api/search/documents?query=fondos%20pensiones&limit=10"

# Buscar normativas sobre empresas
curl "http://localhost:8080/api/search/documents?query=empresa&limit=15"
```

---

## 📚 **Ejemplos de Indexación**

### **Indexación Individual**

```bash
# Subir un documento PDF individual
curl -X POST "http://localhost:8080/api/documents/upload" \
  -F "file=@nueva_normativa.pdf"
```

### **Indexación Masiva**

```bash
# Indexar directorio específico
curl -X POST "http://localhost:8080/api/bulk/index-directory" \
  -d "directoryPath=/Users/usuario/documentos_sii"

# Indexar documentos SII por defecto
curl -X POST "http://localhost:8080/api/bulk/index-sii-documents"

# Indexar documentos de un año específico
curl -X POST "http://localhost:8080/api/bulk/index-year/2020"
```

### **Verificación de Indexación**

```bash
# Verificar estadísticas del índice
curl "http://localhost:8080/api/search/stats"

# Verificar salud del sistema
curl "http://localhost:8080/actuator/health"
```

---

## 🤖 **Scripts de Automatización**

### **Script de Búsqueda Diaria**

```bash
#!/bin/bash
# daily_search.sh - Script para búsquedas diarias automatizadas

API_BASE="http://localhost:8080"
DATE=$(date +%Y-%m-%d)
LOG_FILE="search_log_$DATE.log"

echo "=== Búsqueda Diaria - $DATE ===" >> $LOG_FILE

# Búsqueda de normativas nuevas
echo "Buscando normativas sobre IVA..." >> $LOG_FILE
curl -s "$API_BASE/api/search/documents?query=IVA&limit=5" >> $LOG_FILE

echo "Buscando normativas sobre impuestos..." >> $LOG_FILE
curl -s "$API_BASE/api/search/documents?query=impuesto&limit=5" >> $LOG_FILE

echo "Búsqueda completada" >> $LOG_FILE
```

### **Script de Monitoreo**

```bash
#!/bin/bash
# monitor_api.sh - Script de monitoreo de la API

API_BASE="http://localhost:8080"

echo "=== Monitoreo de API - $(date) ==="

# Verificar salud del servicio
echo "1. Verificando salud del servicio..."
HEALTH=$(curl -s "$API_BASE/actuator/health" | jq -r '.status')
echo "Estado: $HEALTH"

# Verificar estadísticas
echo "2. Verificando estadísticas..."
STATS=$(curl -s "$API_BASE/api/search/stats")
echo "Estadísticas: $STATS"

# Verificar búsqueda básica
echo "3. Verificando búsqueda básica..."
SEARCH=$(curl -s "$API_BASE/api/search/documents?query=test&limit=1")
echo "Búsqueda: $SEARCH"
```

### **Script de Indexación Programada**

```bash
#!/bin/bash
# scheduled_indexing.sh - Script de indexación programada

API_BASE="http://localhost:8080"
SII_DIR="/Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII"

echo "=== Indexación Programada - $(date) ==="

# Indexar documentos SII
echo "Indexando documentos SII..."
RESULT=$(curl -s -X POST "$API_BASE/api/bulk/index-sii-documents")
echo "Resultado: $RESULT"

# Verificar estadísticas después de indexación
echo "Verificando estadísticas..."
STATS=$(curl -s "$API_BASE/api/search/stats")
echo "Estadísticas: $STATS"
```

---

## 🔗 **Integración con Aplicaciones**

### **Python - Ejemplo con requests**

```python
import requests
import json

class SIIAPIClient:
    def __init__(self, base_url="http://localhost:8080"):
        self.base_url = base_url
    
    def search_documents(self, query, limit=10, field="content"):
        """Buscar documentos"""
        url = f"{self.base_url}/api/search/documents"
        params = {
            "query": query,
            "limit": limit,
            "field": field
        }
        response = requests.get(url, params=params)
        return response.json()
    
    def search_by_year(self, year, query, limit=10):
        """Buscar documentos por año"""
        url = f"{self.base_url}/api/search/documents/year/{year}"
        params = {
            "query": query,
            "limit": limit
        }
        response = requests.get(url, params=params)
        return response.json()
    
    def get_stats(self):
        """Obtener estadísticas"""
        url = f"{self.base_url}/api/search/stats"
        response = requests.get(url)
        return response.json()
    
    def upload_document(self, file_path):
        """Subir documento"""
        url = f"{self.base_url}/api/documents/upload"
        with open(file_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(url, files=files)
        return response.text

# Ejemplo de uso
client = SIIAPIClient()

# Buscar documentos
results = client.search_documents("IVA", limit=5)
print(f"Encontrados {results['totalResults']} documentos")

# Buscar por año
year_results = client.search_by_year("2020", "impuesto", limit=10)
print(f"Documentos de 2020: {year_results['totalResults']}")

# Obtener estadísticas
stats = client.get_stats()
print(f"Total de documentos: {stats['totalDocuments']}")
```

### **JavaScript - Ejemplo con fetch**

```javascript
class SIIAPIClient {
    constructor(baseUrl = 'http://localhost:8080') {
        this.baseUrl = baseUrl;
    }
    
    async searchDocuments(query, limit = 10, field = 'content') {
        const url = `${this.baseUrl}/api/search/documents`;
        const params = new URLSearchParams({
            query: query,
            limit: limit,
            field: field
        });
        
        const response = await fetch(`${url}?${params}`);
        return await response.json();
    }
    
    async searchByYear(year, query, limit = 10) {
        const url = `${this.baseUrl}/api/search/documents/year/${year}`;
        const params = new URLSearchParams({
            query: query,
            limit: limit
        });
        
        const response = await fetch(`${url}?${params}`);
        return await response.json();
    }
    
    async getStats() {
        const url = `${this.baseUrl}/api/search/stats`;
        const response = await fetch(url);
        return await response.json();
    }
    
    async uploadDocument(file) {
        const url = `${this.baseUrl}/api/documents/upload`;
        const formData = new FormData();
        formData.append('file', file);
        
        const response = await fetch(url, {
            method: 'POST',
            body: formData
        });
        return await response.text();
    }
}

// Ejemplo de uso
const client = new SIIAPIClient();

// Buscar documentos
client.searchDocuments('IVA', 5).then(results => {
    console.log(`Encontrados ${results.totalResults} documentos`);
    console.log(results.results);
});

// Buscar por año
client.searchByYear('2020', 'impuesto', 10).then(results => {
    console.log(`Documentos de 2020: ${results.totalResults}`);
});

// Obtener estadísticas
client.getStats().then(stats => {
    console.log(`Total de documentos: ${stats.totalDocuments}`);
});
```

### **Java - Ejemplo con Spring WebClient**

```java
@Service
public class SIIAPIService {
    private final WebClient webClient;
    private final String baseUrl = "http://localhost:8080";
    
    public SIIAPIService() {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }
    
    public SearchResponse searchDocuments(String query, int limit, String field) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/search/documents")
                .queryParam("query", query)
                .queryParam("limit", limit)
                .queryParam("field", field)
                .build())
            .retrieve()
            .bodyToMono(SearchResponse.class)
            .block();
    }
    
    public SearchByYearResponse searchByYear(String year, String query, int limit) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/search/documents/year/{year}")
                .queryParam("query", query)
                .queryParam("limit", limit)
                .build(year))
            .retrieve()
            .bodyToMono(SearchByYearResponse.class)
            .block();
    }
    
    public IndexStatsResponse getStats() {
        return webClient.get()
            .uri("/api/search/stats")
            .retrieve()
            .bodyToMono(IndexStatsResponse.class)
            .block();
    }
}
```

---

## 📊 **Casos de Uso Específicos**

### **1. Consulta de Normativas por Contribuyente**

```bash
# Buscar normativas para empresas
curl "http://localhost:8080/api/search/documents?query=empresa&limit=20"

# Buscar normativas para personas naturales
curl "http://localhost:8080/api/search/documents?query=persona%20natural&limit=20"

# Buscar normativas para PYMES
curl "http://localhost:8080/api/search/documents?query=PYME&limit=15"
```

### **2. Consulta por Tipo de Impuesto**

```bash
# IVA
curl "http://localhost:8080/api/search/documents?query=IVA&limit=20"

# Impuesto a la Renta
curl "http://localhost:8080/api/search/documents?query=impuesto%20renta&limit=20"

# Impuesto Territorial
curl "http://localhost:8080/api/search/documents?query=impuesto%20territorial&limit=15"
```

### **3. Consulta por Procedimientos**

```bash
# Declaraciones
curl "http://localhost:8080/api/search/documents?query=declaracion&limit=20"

# Pagos
curl "http://localhost:8080/api/search/documents?query=pago&limit=15"

# Devoluciones
curl "http://localhost:8080/api/search/documents?query=devolucion&limit=15"
```

---

## 🔧 **Troubleshooting**

### **Problemas Comunes**

1. **Servicio no responde**
   ```bash
   # Verificar salud
   curl "http://localhost:8080/actuator/health"
   ```

2. **No hay resultados en búsquedas**
   ```bash
   # Verificar estadísticas
   curl "http://localhost:8080/api/search/stats"
   
   # Re-indexar si es necesario
   curl -X POST "http://localhost:8080/api/bulk/index-sii-documents"
   ```

3. **Error en indexación**
   ```bash
   # Verificar directorio
   curl -X POST "http://localhost:8080/api/bulk/index-directory" \
     -d "directoryPath=/ruta/correcta"
   ```

---

*Para más ejemplos y casos de uso, consulta la [documentación completa de la API](API_DOCUMENTATION.md)*
