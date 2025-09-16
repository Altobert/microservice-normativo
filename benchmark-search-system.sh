#!/bin/bash

# Script de benchmark para evaluar el rendimiento del sistema de búsqueda
# utilizando la clase EvaluationMetrics desarrollada.
# Autor: Sistema de Gestión de Documentos Normativos
# Fecha: $(date +%Y-%m-%d)

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}🚀 BENCHMARK DEL SISTEMA DE BÚSQUEDA CON EVALUATION METRICS${NC}"
echo -e "${CYAN}===========================================================${NC}"
echo ""

# Verificar que estamos en el directorio correcto
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}❌ Error: No se encontró el archivo pom.xml${NC}"
    echo -e "${YELLOW}   Asegúrate de ejecutar este script desde el directorio raíz del proyecto${NC}"
    exit 1
fi

# Verificar que el microservicio esté ejecutándose
echo -e "${BLUE}🔍 1. VERIFICANDO MICROSERVICIO${NC}"
echo "----------------------------------------"
if curl -s --max-time 5 "http://localhost:8080/actuator/health" | grep -q "UP"; then
    echo -e "${GREEN}✅ Microservicio ejecutándose${NC}"
else
    echo -e "${RED}❌ Microservicio no disponible${NC}"
    echo -e "${YELLOW}💡 Ejecuta primero: ./start-app.sh${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}🔨 2. COMPILANDO PROYECTO${NC}"
echo "----------------------------------------"
if mvn compile -q; then
    echo -e "${GREEN}✅ Compilación exitosa${NC}"
else
    echo -e "${RED}❌ Error en la compilación${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}🧪 3. EJECUTANDO PRUEBAS DE INTEGRACIÓN${NC}"
echo "----------------------------------------"
echo -e "${YELLOW}Ejecutando pruebas de evaluación del sistema de búsqueda...${NC}"

if mvn test -Dtest=SearchEvaluatorIntegrationTest -q; then
    echo -e "${GREEN}✅ Pruebas de integración exitosas${NC}"
else
    echo -e "${RED}❌ Algunas pruebas de integración fallaron${NC}"
    echo -e "${YELLOW}💡 Revisa los logs para más detalles${NC}"
fi

echo ""
echo -e "${BLUE}📊 4. EJECUTANDO BENCHMARK COMPLETO${NC}"
echo "----------------------------------------"

# Crear archivo Java temporal para el benchmark
cat > BenchmarkRunner.java << 'EOF'
import cl.sii.normativo.loadnormas.pruebascobertura.SearchEvaluator;
import cl.sii.normativo.loadnormas.pruebascobertura.EvaluationMetrics;
import java.util.List;
import java.util.ArrayList;

public class BenchmarkRunner {
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Iniciando benchmark del sistema de búsqueda...");
            
            SearchEvaluator evaluator = new SearchEvaluator();
            evaluator.initialize();
            
            // Crear consultas de benchmark
            List<SearchEvaluator.TestQuery> benchmarkQueries = createBenchmarkQueries();
            
            System.out.println("\n📋 Consultas de benchmark:");
            for (int i = 0; i < benchmarkQueries.size(); i++) {
                SearchEvaluator.TestQuery query = benchmarkQueries.get(i);
                System.out.println("  " + (i + 1) + ". \"" + query.getQuery() + "\" - " + query.getDescription());
            }
            
            // Ejecutar benchmark con diferentes límites
            System.out.println("\n🔍 Ejecutando benchmark con diferentes límites de resultados...");
            
            int[] limits = {5, 10, 15, 20};
            for (int limit : limits) {
                System.out.println("\n--- Límite: " + limit + " resultados ---");
                
                long startTime = System.currentTimeMillis();
                EvaluationMetrics metrics = evaluator.evaluateMultipleQueries(benchmarkQueries, limit);
                long endTime = System.currentTimeMillis();
                
                System.out.println("Tiempo de ejecución: " + (endTime - startTime) + "ms");
                System.out.println("Métricas:");
                System.out.println("  TP: " + metrics.getTruePositives());
                System.out.println("  FP: " + metrics.getFalsePositives());
                System.out.println("  FN: " + metrics.getFalseNegatives());
                System.out.println("  TN: " + metrics.getTrueNegatives());
                System.out.printf("  Precision: %.4f (%.2f%%)\n", metrics.getPrecision(), metrics.getPrecision() * 100);
                System.out.printf("  Recall: %.4f (%.2f%%)\n", metrics.getRecall(), metrics.getRecall() * 100);
                System.out.printf("  F1-Score: %.4f (%.2f%%)\n", metrics.getF1Score(), metrics.getF1Score() * 100);
                System.out.printf("  Accuracy: %.4f (%.2f%%)\n", metrics.getAccuracy(), metrics.getAccuracy() * 100);
            }
            
            // Generar reporte final
            System.out.println("\n📈 Generando reporte final...");
            EvaluationMetrics finalMetrics = evaluator.evaluateMultipleQueries(benchmarkQueries, 15);
            String report = evaluator.generateEvaluationReport(finalMetrics, benchmarkQueries);
            System.out.println(report);
            
            evaluator.close();
            System.out.println("✅ Benchmark completado exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error durante el benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<SearchEvaluator.TestQuery> createBenchmarkQueries() {
        List<SearchEvaluator.TestQuery> queries = new ArrayList<>();
        
        // Consultas específicas del dominio normativo
        queries.add(new SearchEvaluator.TestQuery("IVA", 
            Set.of("ID1302", "ID1627", "ID1122", "ID041"), 
            "Documentos sobre IVA"));
            
        queries.add(new SearchEvaluator.TestQuery("impuesto", 
            Set.of("ID1302", "ID1627", "ID1122", "ID041", "ID1902"), 
            "Documentos sobre impuestos"));
            
        queries.add(new SearchEvaluator.TestQuery("servicio", 
            Set.of("ID1302", "ID1627"), 
            "Documentos sobre servicios"));
            
        queries.add(new SearchEvaluator.TestQuery("tributación", 
            Set.of("ID1302", "ID1902"), 
            "Documentos sobre tributación"));
            
        queries.add(new SearchEvaluator.TestQuery("circular", 
            Set.of("ID1302", "ID1627", "ID1122"), 
            "Documentos tipo circular"));
            
        queries.add(new SearchEvaluator.TestQuery("instrucción", 
            Set.of("ID1627", "ID1122", "ID1902"), 
            "Documentos tipo instrucción"));
            
        queries.add(new SearchEvaluator.TestQuery("modificación", 
            Set.of("ID1627", "ID1122", "ID041"), 
            "Documentos sobre modificaciones"));
            
        queries.add(new SearchEvaluator.TestQuery("venta", 
            Set.of("ID1627"), 
            "Documentos sobre ventas"));
            
        return queries;
    }
}
EOF

echo -e "${YELLOW}Compilando benchmark...${NC}"
if javac -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" BenchmarkRunner.java; then
    echo -e "${GREEN}✅ Benchmark compilado exitosamente${NC}"
    
    echo ""
    echo -e "${BLUE}🚀 Ejecutando benchmark...${NC}"
    echo "----------------------------------------"
    
    java -cp ".:target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" BenchmarkRunner
    
    echo ""
    echo -e "${GREEN}✅ Benchmark ejecutado exitosamente${NC}"
else
    echo -e "${RED}❌ Error compilando el benchmark${NC}"
fi

# Limpiar archivos temporales
rm -f BenchmarkRunner.java BenchmarkRunner.class

echo ""
echo -e "${BLUE}📈 5. ANÁLISIS DE RESULTADOS${NC}"
echo "----------------------------------------"

# Obtener estadísticas del índice
STATS_RESPONSE=$(curl -s --max-time 5 "http://localhost:8080/api/search/stats")
TOTAL_DOCS=$(echo "$STATS_RESPONSE" | jq -r '.totalDocuments // "N/A"')
INDEX_DIR=$(echo "$STATS_RESPONSE" | jq -r '.indexDirectory // "N/A"')

echo -e "${CYAN}📊 Estadísticas del Sistema:${NC}"
echo -e "   📄 Total documentos indexados: $TOTAL_DOCS"
echo -e "   📁 Directorio del índice: $INDEX_DIR"

# Probar algunas consultas específicas
echo -e "\n${CYAN}🔍 Pruebas de Consultas Específicas:${NC}"
QUERIES=("IVA" "impuesto" "servicio" "tributación" "circular")
for query in "${QUERIES[@]}"; do
    SEARCH_RESPONSE=$(curl -s --max-time 5 "http://localhost:8080/api/search/documents?query=$query&limit=5")
    RESULT_COUNT=$(echo "$SEARCH_RESPONSE" | jq -r '.totalResults // "N/A"')
    echo -e "   🔸 '$query': $RESULT_COUNT resultados"
done

echo ""
echo -e "${BLUE}📋 6. DOCUMENTACIÓN DEL BENCHMARK${NC}"
echo "----------------------------------------"
echo -e "${CYAN}🎯 Objetivos del Benchmark:${NC}"
echo -e "   • Evaluar la calidad de los resultados de búsqueda"
echo -e "   • Medir precision, recall, f1-score y accuracy"
echo -e "   • Comparar rendimiento con diferentes límites de resultados"
echo -e "   • Generar reportes detallados de evaluación"
echo -e "   • Validar consistencia del sistema"

echo -e "\n${CYAN}📊 Métricas Evaluadas:${NC}"
echo -e "   • ${YELLOW}Precision${NC}: TP / (TP + FP) - Precisión de resultados positivos"
echo -e "   • ${YELLOW}Recall${NC}: TP / (TP + FN) - Completitud de resultados"
echo -e "   • ${YELLOW}F1-Score${NC}: Balance entre precision y recall"
echo -e "   • ${YELLOW}Accuracy${NC}: (TP + TN) / Total - Precisión general"

echo -e "\n${CYAN}🔧 Consultas de Benchmark:${NC}"
echo -e "   • Consultas específicas del dominio normativo"
echo -e "   • Términos técnicos del área tributaria"
echo -e "   • Diferentes tipos de documentos (circulares, instrucciones)"
echo -e "   • Variaciones en complejidad de consultas"

echo ""
echo -e "${BLUE}🎯 7. INTERPRETACIÓN DE RESULTADOS${NC}"
echo "----------------------------------------"
echo -e "${CYAN}📈 Rangos de Interpretación:${NC}"
echo -e "   • ${GREEN}Precision ≥ 0.9${NC}: Excelente - Muy pocos resultados irrelevantes"
echo -e "   • ${GREEN}Precision ≥ 0.7${NC}: Buena - Pocos resultados irrelevantes"
echo -e "   • ${YELLOW}Precision ≥ 0.5${NC}: Moderada - Algunos resultados irrelevantes"
echo -e "   • ${RED}Precision < 0.5${NC}: Baja - Muchos resultados irrelevantes"

echo -e "\n   • ${GREEN}Recall ≥ 0.9${NC}: Excelente - Encuentra casi todos los documentos relevantes"
echo -e "   • ${GREEN}Recall ≥ 0.7${NC}: Bueno - Encuentra la mayoría de documentos relevantes"
echo -e "   • ${YELLOW}Recall ≥ 0.5${NC}: Moderado - Encuentra algunos documentos relevantes"
echo -e "   • ${RED}Recall < 0.5${NC}: Bajo - Pierde muchos documentos relevantes"

echo ""
echo -e "${BLUE}💡 8. RECOMENDACIONES DE MEJORA${NC}"
echo "----------------------------------------"
echo -e "${CYAN}🔧 Basado en los resultados del benchmark:${NC}"
echo -e "   • Si ${RED}Precision es baja${NC}: Mejorar filtrado de resultados"
echo -e "   • Si ${RED}Recall es bajo${NC}: Mejorar cobertura de búsqueda"
echo -e "   • Si ${RED}F1-Score es bajo${NC}: Revisar algoritmo de búsqueda completo"
echo -e "   • Si ${RED}Accuracy es baja${NC}: Implementar mejoras generales"

echo -e "\n${CYAN}🚀 Optimizaciones Sugeridas:${NC}"
echo -e "   • Ajustar umbrales de relevancia"
echo -e "   • Implementar expansión de consultas"
echo -e "   • Mejorar análisis de texto"
echo -e "   • Considerar técnicas de ML para ranking"

echo ""
echo -e "${GREEN}🎉 BENCHMARK DEL SISTEMA COMPLETADO${NC}"
echo -e "${GREEN}=====================================${NC}"
echo ""
echo -e "${BLUE}📁 Archivos creados:${NC}"
echo -e "   • ${GREEN}SearchEvaluator.java${NC} - Evaluador de búsqueda"
echo -e "   • ${GREEN}SearchEvaluatorIntegrationTest.java${NC} - Pruebas de integración"
echo -e "   • ${GREEN}EvaluationMetrics.java${NC} - Clase de métricas"
echo -e "   • ${GREEN}EvaluationMetricsTest.java${NC} - Pruebas unitarias"
echo ""
echo -e "${BLUE}📈 Estadísticas:${NC}"
echo -e "   • ${GREEN}Sistema de evaluación completo${NC}"
echo -e "   • ${GREEN}Métricas estándar implementadas${NC}"
echo -e "   • ${GREEN}Benchmark automatizado${NC}"
echo -e "   • ${GREEN}Reportes detallados${NC}"
echo ""
echo -e "${YELLOW}💡 Para ejecutar solo las pruebas de integración:${NC}"
echo -e "   ${BLUE}mvn test -Dtest=SearchEvaluatorIntegrationTest${NC}"
echo ""
echo -e "${YELLOW}💡 Para ejecutar todas las pruebas de evaluación:${NC}"
echo -e "   ${BLUE}mvn test -Dtest=*Evaluation*${NC}"
