#!/bin/bash

# Script completo de pruebas para el Microservicio de Búsqueda de Documentos Normativos SII
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

# Configuración
BASE_URL="http://localhost:8080"
TIMEOUT=10
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Función para mostrar ayuda
show_help() {
    echo -e "${CYAN}🔍 Script de Pruebas del Microservicio de Documentos Normativos SII${NC}"
    echo ""
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help           Mostrar esta ayuda"
    echo "  -v, --verbose        Mostrar output detallado"
    echo "  -q, --quick          Ejecutar solo pruebas básicas"
    echo "  -f, --full           Ejecutar todas las pruebas (por defecto)"
    echo "  -u, --url URL        URL base del microservicio (por defecto: $BASE_URL)"
    echo "  -t, --timeout SEC    Timeout para requests (por defecto: $TIMEOUT)"
    echo ""
    echo "Ejemplos:"
    echo "  $0                   # Ejecutar todas las pruebas"
    echo "  $0 --quick           # Pruebas rápidas"
    echo "  $0 --verbose         # Con output detallado"
    echo "  $0 --url http://localhost:8081  # URL diferente"
}

# Variables por defecto
VERBOSE=false
QUICK_MODE=false
FULL_MODE=true

# Procesar argumentos
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -q|--quick)
            QUICK_MODE=true
            FULL_MODE=false
            shift
            ;;
        -f|--full)
            FULL_MODE=true
            QUICK_MODE=false
            shift
            ;;
        -u|--url)
            BASE_URL="$2"
            shift 2
            ;;
        -t|--timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        *)
            echo -e "${RED}❌ Opción desconocida: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Función para hacer requests con timeout
make_request() {
    local url="$1"
    local method="${2:-GET}"
    local data="${3:-}"
    
    if [ "$VERBOSE" = true ]; then
        echo -e "${BLUE}🔗 $method $url${NC}"
    fi
    
    if [ -n "$data" ]; then
        curl -s --max-time $TIMEOUT -X "$method" -H "Content-Type: application/json" -d "$data" "$url"
    else
        curl -s --max-time $TIMEOUT -X "$method" "$url"
    fi
}

# Función para ejecutar un test
run_test() {
    local test_name="$1"
    local test_command="$2"
    local expected_status="${3:-200}"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$VERBOSE" = true ]; then
        echo -e "${YELLOW}🧪 Ejecutando: $test_name${NC}"
    fi
    
    local result=$(eval "$test_command")
    local status_code=$(echo "$result" | tail -n1)
    
    if [ "$status_code" = "$expected_status" ]; then
        echo -e "${GREEN}✅ $test_name${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo -e "${RED}❌ $test_name (Status: $status_code, Esperado: $expected_status)${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# Función para verificar JSON válido
check_json() {
    local json_string="$1"
    echo "$json_string" | jq -e . > /dev/null 2>&1
}

# Función para extraer valor de JSON
get_json_value() {
    local json_string="$1"
    local key="$2"
    echo "$json_string" | jq -r "$key" 2>/dev/null || echo "null"
}

# Header del script
echo -e "${CYAN}🔍 PRUEBAS COMPLETAS DEL MICROSERVICIO DE DOCUMENTOS NORMATIVOS SII${NC}"
echo -e "${CYAN}================================================================${NC}"
echo ""
echo -e "${BLUE}📋 Configuración:${NC}"
echo -e "   🌐 URL Base: $BASE_URL"
echo -e "   ⏱️  Timeout: ${TIMEOUT}s"
echo -e "   🚀 Modo: $([ "$QUICK_MODE" = true ] && echo "Rápido" || echo "Completo")"
echo -e "   📊 Verbose: $([ "$VERBOSE" = true ] && echo "Sí" || echo "No")"
echo ""

# Verificar que curl y jq estén disponibles
if ! command -v curl &> /dev/null; then
    echo -e "${RED}❌ Error: curl no está instalado${NC}"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo -e "${RED}❌ Error: jq no está instalado${NC}"
    echo -e "${YELLOW}💡 Instala jq con: brew install jq${NC}"
    exit 1
fi

echo -e "${PURPLE}🔧 1. VERIFICACIÓN DE CONECTIVIDAD${NC}"
echo -e "${PURPLE}=====================================${NC}"

# Test 1: Verificar que el microservicio esté ejecutándose
run_test "Microservicio ejecutándose" "make_request '$BASE_URL/actuator/health' | jq -r '.status' | grep -q 'UP' && echo '200' || echo '500'"

# Test 2: Verificar respuesta básica
run_test "Respuesta HTTP básica" "make_request '$BASE_URL/api/documents/info' | jq -e '.service' > /dev/null && echo '200' || echo '500'"

echo ""
echo -e "${PURPLE}📊 2. PRUEBAS DE INFORMACIÓN DEL SERVICIO${NC}"
echo -e "${PURPLE}===========================================${NC}"

# Test 3: Información del servicio
run_test "Información del servicio" "make_request '$BASE_URL/api/documents/info' | jq -e '.service and .version and .description' > /dev/null && echo '200' || echo '500'"

# Test 4: Estado del servicio
run_test "Estado del servicio" "make_request '$BASE_URL/api/documents/status' | jq -e '.status and .service' > /dev/null && echo '200' || echo '500'"

echo ""
echo -e "${PURPLE}📈 3. PRUEBAS DE ESTADÍSTICAS${NC}"
echo -e "${PURPLE}=============================${NC}"

# Test 5: Estadísticas del índice
run_test "Estadísticas del índice" "make_request '$BASE_URL/api/search/stats' | jq -e '.totalDocuments and .indexDirectory' > /dev/null && echo '200' || echo '500'"

# Test 6: Verificar que hay documentos indexados
run_test "Documentos indexados disponibles" "make_request '$BASE_URL/api/search/stats' | jq -e '.totalDocuments > 0' > /dev/null && echo '200' || echo '500'"

echo ""
echo -e "${PURPLE}🔍 4. PRUEBAS DE BÚSQUEDA BÁSICA${NC}"
echo -e "${PURPLE}===================================${NC}"

# Test 7: Búsqueda básica
run_test "Búsqueda básica" "make_request '$BASE_URL/api/search/documents?query=IVA&limit=5' | jq -e '.results and .totalResults' > /dev/null && echo '200' || echo '500'"

# Test 8: Búsqueda con límite
run_test "Búsqueda con límite" "make_request '$BASE_URL/api/search/documents?query=impuesto&limit=3' | jq -e '.limit == 3' > /dev/null && echo '200' || echo '500'"

# Test 9: Búsqueda por campo específico
run_test "Búsqueda por campo" "make_request '$BASE_URL/api/search/documents?query=IVA&field=content&limit=5' | jq -e '.field == \"content\"' > /dev/null && echo '200' || echo '500'"

if [ "$QUICK_MODE" = false ]; then
    echo ""
    echo -e "${PURPLE}🔍 5. PRUEBAS DE BÚSQUEDA AVANZADA${NC}"
    echo -e "${PURPLE}=====================================${NC}"

    # Test 10: Búsqueda por año
    run_test "Búsqueda por año" "make_request '$BASE_URL/api/search/documents/year/2020?query=IVA&limit=5' | jq -e '.year == \"2020\"' > /dev/null && echo '200' || echo '500'"

    # Test 11: Búsqueda por ID de documento
    run_test "Búsqueda por ID" "make_request '$BASE_URL/api/search/documents/id/ID1302' | jq -e '.documentId == \"ID1302\"' > /dev/null && echo '200' || echo '500'"

    # Test 12: Búsqueda con términos múltiples
    run_test "Búsqueda múltiples términos" "make_request '$BASE_URL/api/search/documents?query=IVA+impuesto&limit=5' | jq -e '.results' > /dev/null && echo '200' || echo '500'"

    echo ""
    echo -e "${PURPLE}🚨 6. PRUEBAS DE MANEJO DE ERRORES${NC}"
    echo -e "${PURPLE}=====================================${NC}"

    # Test 13: Búsqueda con query vacía
    run_test "Query vacía (debe manejar error)" "make_request '$BASE_URL/api/search/documents?query=&limit=5' | jq -e '.error or .results' > /dev/null && echo '200' || echo '500'"

    # Test 14: Límite inválido
    run_test "Límite inválido" "make_request '$BASE_URL/api/search/documents?query=IVA&limit=-1' | jq -e '.results or .error' > /dev/null && echo '200' || echo '500'"

    # Test 15: Campo inexistente
    run_test "Campo inexistente" "make_request '$BASE_URL/api/search/documents?query=IVA&field=campo_inexistente&limit=5' | jq -e '.results or .error' > /dev/null && echo '200' || echo '500'"

    echo ""
    echo -e "${PURPLE}📋 7. PRUEBAS DE ESTRUCTURA DE RESPUESTA${NC}"
    echo -e "${PURPLE}===========================================${NC}"

    # Test 16: Estructura de resultado de búsqueda
    run_test "Estructura de resultado" "make_request '$BASE_URL/api/search/documents?query=IVA&limit=1' | jq -e '.results[0] | has(\"score\") and has(\"filename\") and has(\"documentId\")' > /dev/null && echo '200' || echo '500'"

    # Test 17: Campos requeridos en resultado
    run_test "Campos requeridos" "make_request '$BASE_URL/api/search/documents?query=IVA&limit=1' | jq -e '.results[0] | has(\"title\") and has(\"filepath\") and has(\"snippet\")' > /dev/null && echo '200' || echo '500'"

    echo ""
    echo -e "${PURPLE}🌐 8. PRUEBAS DE DOCUMENTACIÓN API${NC}"
    echo -e "${PURPLE}=====================================${NC}"

    # Test 18: Documentación OpenAPI
    run_test "Documentación OpenAPI" "make_request '$BASE_URL/api-docs' | jq -e '.info and .paths' > /dev/null && echo '200' || echo '500'"

    # Test 19: Swagger UI disponible
    run_test "Swagger UI disponible" "make_request '$BASE_URL/swagger-ui/index.html' | grep -q 'Swagger UI' && echo '200' || echo '500'"

    echo ""
    echo -e "${PURPLE}⚡ 9. PRUEBAS DE RENDIMIENTO${NC}"
    echo -e "${PURPLE}=============================${NC}"

    # Test 20: Tiempo de respuesta de búsqueda
    local start_time=$(date +%s%3N)
    make_request "$BASE_URL/api/search/documents?query=IVA&limit=10" > /dev/null
    local end_time=$(date +%s%3N)
    local response_time=$((end_time - start_time))
    
    if [ $response_time -lt 2000 ]; then
        echo -e "${GREEN}✅ Tiempo de respuesta aceptable (${response_time}ms)${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ Tiempo de respuesta lento (${response_time}ms)${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    # Test 21: Tiempo de respuesta de estadísticas
    start_time=$(date +%s%3N)
    make_request "$BASE_URL/api/search/stats" > /dev/null
    end_time=$(date +%s%3N)
    response_time=$((end_time - start_time))
    
    if [ $response_time -lt 1000 ]; then
        echo -e "${GREEN}✅ Estadísticas rápidas (${response_time}ms)${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ Estadísticas lentas (${response_time}ms)${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
fi

echo ""
echo -e "${PURPLE}📊 10. ANÁLISIS DE RESULTADOS${NC}"
echo -e "${PURPLE}=============================${NC}"

# Obtener estadísticas del índice
STATS_RESPONSE=$(make_request "$BASE_URL/api/search/stats")
TOTAL_DOCS=$(get_json_value "$STATS_RESPONSE" ".totalDocuments")
INDEX_DIR=$(get_json_value "$STATS_RESPONSE" ".indexDirectory")

echo -e "${BLUE}📈 Estadísticas del Índice:${NC}"
echo -e "   📄 Total documentos: $TOTAL_DOCS"
echo -e "   📁 Directorio índice: $INDEX_DIR"

# Probar diferentes términos de búsqueda
echo -e "${BLUE}🔍 Términos de búsqueda probados:${NC}"
SEARCH_TERMS=("IVA" "impuesto" "tributación" "servicio" "venta")
for term in "${SEARCH_TERMS[@]}"; do
    SEARCH_RESPONSE=$(make_request "$BASE_URL/api/search/documents?query=$term&limit=1")
    RESULT_COUNT=$(get_json_value "$SEARCH_RESPONSE" ".totalResults")
    echo -e "   🔸 '$term': $RESULT_COUNT resultados"
done

echo ""
echo -e "${CYAN}📋 RESUMEN DE PRUEBAS${NC}"
echo -e "${CYAN}====================${NC}"
echo -e "${GREEN}✅ Pruebas exitosas: $PASSED_TESTS${NC}"
echo -e "${RED}❌ Pruebas fallidas: $FAILED_TESTS${NC}"
echo -e "${BLUE}📊 Total de pruebas: $TOTAL_TESTS${NC}"

# Calcular porcentaje de éxito
if [ $TOTAL_TESTS -gt 0 ]; then
    SUCCESS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo -e "${BLUE}📈 Tasa de éxito: $SUCCESS_RATE%${NC}"
    
    if [ $SUCCESS_RATE -ge 90 ]; then
        echo -e "${GREEN}🎉 ¡Excelente! El microservicio está funcionando correctamente${NC}"
    elif [ $SUCCESS_RATE -ge 70 ]; then
        echo -e "${YELLOW}⚠️  Bueno, pero hay algunas áreas de mejora${NC}"
    else
        echo -e "${RED}🚨 Hay problemas significativos que requieren atención${NC}"
    fi
fi

echo ""
echo -e "${CYAN}🔗 URLs Útiles:${NC}"
echo -e "   🌐 Swagger UI: $BASE_URL/swagger-ui/index.html"
echo -e "   📖 API Docs: $BASE_URL/api-docs"
echo -e "   🔍 Búsqueda: $BASE_URL/api/search/documents?query=IVA&limit=5"
echo -e "   📊 Estadísticas: $BASE_URL/api/search/stats"
echo -e "   ❤️  Health Check: $BASE_URL/actuator/health"

echo ""
echo -e "${CYAN}🎯 Próximos pasos:${NC}"
if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "   ✅ Todas las pruebas pasaron - El microservicio está listo para producción"
else
    echo -e "   🔧 Revisar las pruebas fallidas y corregir los problemas"
    echo -e "   📝 Verificar logs de la aplicación para más detalles"
fi

echo ""
echo -e "${CYAN}🏁 Pruebas completadas el $(date)${NC}"
