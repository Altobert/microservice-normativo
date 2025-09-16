# 📊 Sistema de Evaluación de Pruebas de Cobertura

Este directorio contiene el sistema completo de evaluación de rendimiento para el microservicio de búsqueda de documentos normativos.

## 📁 Estructura de Archivos

```
pruebascobertura/
├── EvaluationMetrics.java           # Clase principal de métricas
├── EvaluationMetricsExample.java    # Ejemplos de uso
├── SearchEvaluator.java             # Evaluador de búsqueda
└── README.md                        # Esta documentación
```

## 🧪 Pruebas

```
src/test/java/cl/sii/normativo/loadnormas/pruebascobertura/
├── EvaluationMetricsTest.java           # 31 pruebas unitarias
└── SearchEvaluatorIntegrationTest.java  # 7 pruebas de integración
```

## 🚀 Scripts de Ejecución

```
├── test-evaluation-metrics.sh       # Pruebas de EvaluationMetrics
├── benchmark-search-system.sh       # Benchmark completo del sistema
└── test-microservice.sh             # Pruebas del microservicio
```

## 📊 Métricas Implementadas

### Precision
- **Fórmula**: `TP / (TP + FP)`
- **Interpretación**: Porcentaje de resultados positivos que son correctos
- **Rango**: 0.0 - 1.0

### Recall
- **Fórmula**: `TP / (TP + FN)`
- **Interpretación**: Porcentaje de documentos relevantes encontrados
- **Rango**: 0.0 - 1.0

### F1-Score
- **Fórmula**: `2 * (Precision * Recall) / (Precision + Recall)`
- **Interpretación**: Balance entre precision y recall
- **Rango**: 0.0 - 1.0

### Accuracy
- **Fórmula**: `(TP + TN) / (TP + TN + FP + FN)`
- **Interpretación**: Porcentaje total de predicciones correctas
- **Rango**: 0.0 - 1.0

## 🎯 Casos de Uso

### 1. Evaluación de Rendimiento
```java
SearchEvaluator evaluator = new SearchEvaluator();
evaluator.initialize();

List<SearchEvaluator.TestQuery> queries = evaluator.createTestQueries();
EvaluationMetrics metrics = evaluator.evaluateMultipleQueries(queries, 15);

System.out.println("Precision: " + metrics.getPrecision());
System.out.println("Recall: " + metrics.getRecall());
```

### 2. Monitoreo Continuo
```java
@Scheduled(fixedRate = 3600000) // Cada hora
public void monitorQuality() {
    EvaluationMetrics metrics = evaluator.evaluateMultipleQueries(queries, 10);
    
    if (metrics.getPrecision() < 0.7) {
        alertService.sendAlert("Precision baja: " + metrics.getPrecision());
    }
}
```

### 3. Comparación de Configuraciones
```java
// Evaluar diferentes límites
EvaluationMetrics metrics5 = evaluator.evaluateMultipleQueries(queries, 5);
EvaluationMetrics metrics10 = evaluator.evaluateMultipleQueries(queries, 10);
EvaluationMetrics metrics20 = evaluator.evaluateMultipleQueries(queries, 20);

// Comparar resultados
System.out.println("Mejor límite para precision: " + 
    (metrics5.getPrecision() > metrics10.getPrecision() ? "5" : "10"));
```

## 🔧 Ejecución de Pruebas

### Pruebas Unitarias
```bash
# Solo EvaluationMetrics
mvn test -Dtest=EvaluationMetricsTest

# Con output detallado
mvn test -Dtest=EvaluationMetricsTest -X
```

### Pruebas de Integración
```bash
# Solo SearchEvaluator
mvn test -Dtest=SearchEvaluatorIntegrationTest

# Todas las pruebas de evaluación
mvn test -Dtest=*Evaluation*
```

### Benchmark Completo
```bash
# Ejecutar benchmark automatizado
./benchmark-search-system.sh

# Pruebas rápidas
./test-evaluation-metrics.sh
```

## 📈 Resultados Actuales

### Métricas del Sistema
- **Precision**: 10.81% (Baja - muchos resultados irrelevantes)
- **Recall**: 50.00% (Moderado - encuentra algunos documentos relevantes)
- **F1-Score**: 17.78% (Bajo - necesita mejora)
- **Accuracy**: 85.77% (Bueno - sistema preciso en general)

### Recomendaciones
1. **Mejorar filtrado** para aumentar precision
2. **Expandir cobertura** para aumentar recall
3. **Revisar algoritmo** de búsqueda completo
4. **Implementar técnicas** de machine learning

## 🛠️ Configuración

### Requisitos
- Java 17+
- Maven 3.6+
- Microservicio ejecutándose en puerto 8080
- Índice de Lucene disponible

### Configuración del Índice
```properties
# application.properties
lucene.index.directory=/path/to/lucene-index
```

### Consultas Personalizadas
```java
// Crear consultas específicas
List<SearchEvaluator.TestQuery> customQueries = Arrays.asList(
    new SearchEvaluator.TestQuery("tu_consulta", 
        Set.of("doc1", "doc2"), 
        "Descripción")
);
```

## 📚 Documentación Adicional

- **[Documentación Completa](docs/soluciones/SISTEMA_EVALUACION_METRICAS.md)** - Guía detallada del sistema
- **[Scripts de Gestión](SCRIPTS_README.md)** - Documentación de scripts
- **[API Documentation](docs/api/)** - Documentación de la API

## 🔍 Troubleshooting

### Problemas Comunes

1. **Error de directorio del índice**
   ```bash
   # Verificar que existe
   ls -la /path/to/lucene-index
   ```

2. **Microservicio no disponible**
   ```bash
   # Iniciar microservicio
   ./start-app.sh
   ```

3. **Métricas inconsistentes**
   ```java
   // Verificar consistencia
   EvaluationMetrics m1 = evaluator.evaluateSearch(query, 10);
   EvaluationMetrics m2 = evaluator.evaluateSearch(query, 10);
   assertEquals(m1.getTruePositives(), m2.getTruePositives());
   ```

## 🎯 Próximos Pasos

1. **Implementar ML** para ranking automático
2. **Crear dashboard** de métricas en tiempo real
3. **Integrar con CI/CD** para monitoreo continuo
4. **Expandir métricas** con NDCG, MAP, etc.

---

*Sistema de Evaluación de Pruebas de Cobertura v1.0 - $(date +%Y-%m-%d)*
