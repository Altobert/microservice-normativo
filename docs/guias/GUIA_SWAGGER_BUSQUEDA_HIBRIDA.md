# 🌐 Guía de Uso de Swagger para Búsqueda Híbrida

## 📋 Resumen Ejecutivo

Esta guía te ayudará a utilizar la interfaz de Swagger UI para probar y explorar los nuevos endpoints de búsqueda híbrida del microservicio de documentos normativos SII.

## 🚀 Acceso a Swagger UI

### URL Principal
```
http://localhost:8080/swagger-ui.html
```

### Documentación de API
```
http://localhost:8080/api-docs
```

## 🔍 Endpoints de Búsqueda Híbrida Disponibles

### 1. 📊 Estadísticas del Sistema Híbrido
**Endpoint**: `GET /api/search/hybrid-stats`

**Descripción**: Obtiene información detallada sobre el estado del sistema de búsqueda híbrida.

**Parámetros**: Ninguno

**Ejemplo de respuesta**:
```json
{
  "hybridSearch": {
    "embeddingCacheSize": 0,
    "embeddingServiceAvailable": true,
    "indexDirectory": "/path/to/index",
    "supportedFields": ["content", "vector", "filename", "title"],
    "searchType": "hybrid"
  },
  "vectorIndexer": {
    "hasVectors": true,
    "embeddingServiceAvailable": true,
    "indexDirectory": "/path/to/index",
    "totalDocuments": 104,
    "embeddingCacheSize": 0
  },
  "timestamp": 1758035817771
}
```

### 2. 🔍 Búsqueda Híbrida Básica
**Endpoint**: `GET /api/search/hybrid`

**Descripción**: Realiza una búsqueda que combina búsqueda tradicional y vectorial con pesos configurables.

**Parámetros**:
- `query` (requerido): Consulta de búsqueda
- `limit` (opcional): Número máximo de resultados (default: 10)
- `traditionalWeight` (opcional): Peso para búsqueda tradicional (0.0-1.0, default: 0.6)
- `vectorWeight` (opcional): Peso para búsqueda vectorial (0.0-1.0, default: 0.4)

**Ejemplos de uso**:

#### Consulta específica (más peso tradicional)
```
query: IVA
limit: 5
traditionalWeight: 0.7
vectorWeight: 0.3
```

#### Consulta semántica (más peso vectorial)
```
query: impuestos sobre la renta de personas naturales
limit: 10
traditionalWeight: 0.3
vectorWeight: 0.7
```

#### Consulta balanceada
```
query: obligaciones tributarias
limit: 8
traditionalWeight: 0.5
vectorWeight: 0.5
```

### 3. 🧠 Búsqueda Híbrida Inteligente
**Endpoint**: `GET /api/search/smart-hybrid`

**Descripción**: Realiza una búsqueda híbrida con pesos automáticos basados en las características de la consulta.

**Parámetros**:
- `query` (requerido): Consulta de búsqueda
- `limit` (opcional): Número máximo de resultados (default: 10)

**Algoritmo de pesos automáticos**:
- Consultas cortas (≤50 chars): traditionalWeight=0.6, vectorWeight=0.4
- Consultas largas (>50 chars): traditionalWeight=0.4, vectorWeight=0.6
- Palabras muy cortas (≤3 chars): traditionalWeight=0.7, vectorWeight=0.3

## 📝 Ejemplos de Consultas para Probar

### 🔍 Búsquedas Básicas
- `IVA`
- `impuestos`
- `renta`
- `contribuyente`
- `declaración`
- `fiscal`

### 🧠 Búsquedas Semánticas
- `impuestos sobre la renta`
- `declaración de impuestos`
- `obligaciones tributarias`
- `régimen fiscal`
- `exenciones tributarias`
- `personas naturales`
- `empresas tributarias`

### ⚖️ Configuraciones de Pesos Recomendadas

#### Para Consultas Específicas
```
traditionalWeight: 0.8
vectorWeight: 0.2
```
**Uso**: Cuando buscas términos exactos como "IVA", "SII", códigos específicos.

#### Para Consultas Balanceadas
```
traditionalWeight: 0.5
vectorWeight: 0.5
```
**Uso**: Cuando quieres un equilibrio entre precisión y relevancia semántica.

#### Para Consultas Semánticas
```
traditionalWeight: 0.2
vectorWeight: 0.8
```
**Uso**: Cuando buscas conceptos amplios o frases descriptivas.

## 📊 Interpretación de Resultados

### Estructura de Respuesta
```json
{
  "query": "consulta de ejemplo",
  "limit": 10,
  "traditionalWeight": 0.6,
  "vectorWeight": 0.4,
  "totalResults": 3,
  "results": [
    {
      "documentId": "ID1234",
      "filename": "documento.pdf",
      "title": "Título del documento",
      "year": "2024",
      "traditionalScore": 1.2345955,
      "vectorScore": 0.8765432,
      "hybridScore": 1.0555555,
      "matchType": "hybrid",
      "snippet": "Contenido del documento..."
    }
  ]
}
```

### Campos de Resultado

| Campo | Descripción |
|-------|-------------|
| `documentId` | ID único del documento |
| `filename` | Nombre del archivo PDF |
| `title` | Título del documento |
| `year` | Año del documento |
| `traditionalScore` | Score de búsqueda tradicional (TF-IDF) |
| `vectorScore` | Score de búsqueda vectorial (similitud semántica) |
| `hybridScore` | Score combinado final |
| `matchType` | Tipo de coincidencia: `traditional`, `vector`, `hybrid` |
| `snippet` | Fragmento del contenido del documento |

### Tipos de Match

- **`traditional`**: Solo encontrado en búsqueda tradicional
- **`vector`**: Solo encontrado en búsqueda vectorial
- **`hybrid`**: Encontrado en ambas búsquedas

## 🧪 Casos de Prueba Recomendados

### 1. Comparación de Métodos
**Objetivo**: Comparar resultados entre búsqueda tradicional y híbrida.

**Pasos**:
1. Usar búsqueda tradicional: `/api/search/documents?query=IVA&limit=5`
2. Usar búsqueda híbrida: `/api/search/hybrid?query=IVA&limit=5`
3. Comparar resultados y scores

### 2. Prueba de Pesos Diferentes
**Objetivo**: Ver cómo afectan los pesos a los resultados.

**Pasos**:
1. Consulta base: `impuestos sobre la renta`
2. Probar con traditionalWeight=0.8, vectorWeight=0.2
3. Probar con traditionalWeight=0.2, vectorWeight=0.8
4. Comparar diferencias en resultados

### 3. Prueba de Búsqueda Inteligente
**Objetivo**: Verificar el ajuste automático de pesos.

**Pasos**:
1. Consulta corta: `/api/search/smart-hybrid?query=IVA&limit=5`
2. Consulta larga: `/api/search/smart-hybrid?query=impuestos sobre la renta de las personas naturales&limit=5`
3. Observar cómo cambian los pesos automáticamente

### 4. Prueba de Límites
**Objetivo**: Verificar el comportamiento con diferentes límites.

**Pasos**:
1. Probar con limit=3 (pocos resultados)
2. Probar con limit=10 (resultados estándar)
3. Probar con limit=20 (muchos resultados)

## 🔧 Troubleshooting en Swagger

### Problemas Comunes

#### 1. Error 500 - Servicio no disponible
**Solución**: Verificar que el microservicio esté ejecutándose
```bash
curl http://localhost:8080/actuator/health
```

#### 2. Resultados vacíos
**Posibles causas**:
- Índice no inicializado
- Consulta muy específica
- Problemas con el directorio de índice

**Solución**: Verificar estadísticas del sistema
```bash
curl http://localhost:8080/api/search/hybrid-stats
```

#### 3. Scores vectoriales en 0.0
**Explicación**: Esto es normal en la implementación actual, ya que usa embeddings simplificados.

#### 4. Pesos no se aplican correctamente
**Verificación**: Los pesos se normalizan automáticamente si suman más de 1.0.

## 📈 Métricas de Rendimiento

### Benchmarks Esperados

| Tipo de Consulta | Tiempo Promedio | Resultados Típicos |
|------------------|-----------------|-------------------|
| Consulta corta | 50-100ms | 3-5 resultados |
| Consulta media | 100-200ms | 5-10 resultados |
| Consulta larga | 150-300ms | 8-15 resultados |

### Optimizaciones Observables

- **Cache de embeddings**: Mejora el rendimiento en consultas repetidas
- **Pesos automáticos**: Optimiza resultados sin configuración manual
- **Límites inteligentes**: Balancea precisión vs. rendimiento

## 🎯 Mejores Prácticas

### 1. Selección de Consultas
- **Específicas**: Usa términos exactos para búsqueda tradicional
- **Semánticas**: Usa frases descriptivas para búsqueda vectorial
- **Balanceadas**: Combina ambos enfoques según necesidad

### 2. Configuración de Pesos
- **70/30**: Para consultas muy específicas
- **50/50**: Para uso general
- **30/70**: Para consultas conceptuales

### 3. Interpretación de Resultados
- **traditionalScore**: Confianza en coincidencia exacta
- **vectorScore**: Confianza en similitud semántica
- **hybridScore**: Confianza combinada final
- **matchType**: Indica el origen del resultado

## 🔗 Enlaces Útiles

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **Estadísticas**: http://localhost:8080/api/search/hybrid-stats

## 📚 Documentación Relacionada

- [Sistema de Búsqueda Vectorial Híbrida](../soluciones/BUSQUEDA_VECTORIAL_HIBRIDA.md)
- [Guía de Indexación de Documentos](GUIA_INDEXACION_DOCUMENTOS.md)
- [Scripts de Pruebas](../scripts/README.md)

---

**🎉 ¡Disfruta explorando la búsqueda híbrida en Swagger UI!**
