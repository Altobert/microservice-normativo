# 🔍 Microservicio de Búsqueda de Documentos Normativos SII

Microservicio especializado en búsqueda de texto completo de documentos normativos del Servicio de Impuestos Internos (SII) utilizando Apache Lucene. Este servicio se enfoca exclusivamente en proporcionar capacidades de búsqueda sobre índices previamente creados.

## 🚀 **Inicio Rápido**

### Ejecutar la aplicación:
```bash
mvn spring-boot:run -Dmaven.test.skip=true
```

### Nota importante:
Este servicio es solo para búsquedas. Para indexar documentos, usa el servicio pipeline separado.

### Acceder a Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

## 📚 **Documentación Completa**

Toda la documentación está organizada en la carpeta `docs/`:

- **[📖 Documentación Principal](docs/README.md)** - Índice completo de toda la documentación
- **[🛠️ Soluciones](docs/soluciones/)** - Soluciones implementadas para problemas específicos
- **[📊 Sistema de Evaluación](docs/soluciones/SISTEMA_EVALUACION_METRICAS.md)** - Evaluación de rendimiento con métricas
- **[📜 Scripts](docs/scripts/)** - Scripts de automatización y utilidades
- **[🔌 API](docs/api/)** - Documentación de la API y Swagger
- **[📖 Guías](docs/guias/)** - Guías de uso y procedimientos
- **[🔧 Problemas Resueltos](docs/problemas-resueltos/)** - Documentación técnica detallada

## ✅ **Estado Actual**

- ✅ **Servicio de búsqueda funcionando correctamente**
- ✅ **API Swagger disponible**
- ✅ **Separación completa del pipeline de indexación**
- ✅ **Enfoque exclusivo en búsquedas**
- ✅ **Sistema de evaluación de rendimiento implementado**
- ✅ **Métricas de calidad de búsqueda automatizadas**

## 🔗 **Endpoints Principales**

- **Health Check**: `http://localhost:8080/actuator/health`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/api-docs`
- **Búsqueda**: `http://localhost:8080/api/search/documents?query=impuesto`
- **Búsqueda por año**: `http://localhost:8080/api/search/documents/year/2020?query=impuesto`
- **Estadísticas**: `http://localhost:8080/api/search/stats`

## 📊 **Sistema de Evaluación de Rendimiento**

El microservicio incluye un sistema completo de evaluación de rendimiento que permite medir objetivamente la calidad de los resultados de búsqueda:

### Métricas Implementadas
- **Precision**: Precisión de resultados positivos (TP / (TP + FP))
- **Recall**: Completitud de resultados (TP / (TP + FN))
- **F1-Score**: Balance entre precision y recall
- **Accuracy**: Precisión general del sistema

### Scripts de Evaluación
```bash
# Benchmark completo del sistema
./benchmark-search-system.sh

# Pruebas de métricas de evaluación
./test-evaluation-metrics.sh

# Pruebas del microservicio
./test-microservice.sh
```

### Resultados Actuales
- **Precision**: 10.81% (Identifica necesidad de mejorar filtrado)
- **Recall**: 50.00% (Encuentra la mitad de documentos relevantes)
- **F1-Score**: 17.78% (Balance que requiere optimización)
- **Accuracy**: 85.77% (Sistema preciso en general)

### Documentación
- **[Sistema de Evaluación Completo](docs/soluciones/SISTEMA_EVALUACION_METRICAS.md)**
- **[README de Pruebas de Cobertura](src/main/java/cl/sii/normativo/loadnormas/pruebascobertura/README.md)**

## 🛠️ **Tecnologías**

- **Java 17**
- **Spring Boot 3.4.5**
- **Apache Lucene 9.6.0**
- **SpringDoc OpenAPI 3**
- **JUnit 5** (Pruebas unitarias e integración)
- **Maven** (Gestión de dependencias)

## 🏗️ **Arquitectura**

Este microservicio forma parte de una arquitectura de microservicios separada:

- **🔍 Este servicio**: Búsquedas de texto completo
- **⚙️ Pipeline separado**: Indexación y procesamiento de documentos

**Beneficios de la separación**:
- ✅ **Especialización**: Cada servicio tiene una responsabilidad específica
- ✅ **Escalabilidad**: Servicios independientes pueden escalarse por separado
- ✅ **Mantenibilidad**: Código más organizado y fácil de mantener
- ✅ **Despliegue**: Despliegue independiente de servicios

---

*Para más detalles, consulta la [documentación completa](docs/README.md)*
