#!/bin/bash

# 🚀 Script de Pruebas de Búsqueda Híbrida en Swagger
# Este script automatiza las pruebas de los endpoints híbridos desde Swagger UI

set -e

# Configuración
BASE_URL="http://localhost:8080"
SWAGGER_URL="$BASE_URL/swagger-ui.html"
API_BASE="$BASE_URL/api/search"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Función para imprimir con colores
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_swagger() {
    echo -e "${PURPLE}[SWAGGER]${NC} $1"
}

# Función para hacer requests HTTP y mostrar resultados
make_request() {
    local endpoint="$1"
    local description="$2"
    local expected_status="${3:-200}"
    
    print_status "Probando: $description"
    print_status "Endpoint: $endpoint"
    
    response=$(curl -s -w "\n%{http_code}" "$endpoint" || echo "000")
    http_code=$(echo "$response" | tail -1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ]; then
        print_success "✓ $description - Status: $http_code"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
        echo ""
    else
        print_error "✗ $description - Status: $http_code (esperado: $expected_status)"
        echo "$body"
        echo ""
    fi
}

# Función para verificar si el servicio está ejecutándose
check_service() {
    print_status "Verificando si el microservicio está ejecutándose..."
    
    if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        print_success "✓ Microservicio está ejecutándose"
        return 0
    else
        print_error "✗ Microservicio no está ejecutándose en $BASE_URL"
        print_warning "Ejecuta: ./start-hybrid-app.sh"
        return 1
    fi
}

# Función para mostrar información de Swagger
show_swagger_info() {
    print_swagger "🌐 Swagger UI disponible en: $SWAGGER_URL"
    print_swagger "📚 Documentación de API en: $BASE_URL/api-docs"
    echo ""
    print_swagger "🔍 Endpoints de búsqueda híbrida disponibles:"
    echo ""
    echo "1. 📊 Estadísticas del sistema híbrido:"
    echo "   GET $API_BASE/hybrid-stats"
    echo ""
    echo "2. 🔍 Búsqueda híbrida básica:"
    echo "   GET $API_BASE/hybrid?query=impuestos&limit=10&traditionalWeight=0.6&vectorWeight=0.4"
    echo ""
    echo "3. 🧠 Búsqueda híbrida inteligente:"
    echo "   GET $API_BASE/smart-hybrid?query=impuestos sobre la renta&limit=10"
    echo ""
    echo "4. 📈 Comparación con búsqueda tradicional:"
    echo "   GET $API_BASE/documents?query=impuestos&limit=10"
    echo ""
}

# Función para ejecutar pruebas automáticas
run_automatic_tests() {
    print_status "🚀 Ejecutando pruebas automáticas de búsqueda híbrida..."
    echo ""
    
    echo "=========================================="
    print_status "📊 PRUEBAS DE ESTADÍSTICAS HÍBRIDAS"
    echo "=========================================="
    
    # Estadísticas del sistema híbrido
    make_request "$API_BASE/hybrid-stats" "Estadísticas del sistema híbrido"
    
    echo "=========================================="
    print_status "🔍 PRUEBAS DE BÚSQUEDA HÍBRIDA"
    echo "=========================================="
    
    # Búsqueda híbrida básica
    make_request "$API_BASE/hybrid?query=IVA&limit=5" "Búsqueda híbrida básica - IVA"
    
    # Búsqueda híbrida con pesos personalizados
    make_request "$API_BASE/hybrid?query=impuestos&limit=3&traditionalWeight=0.7&vectorWeight=0.3" "Búsqueda híbrida con más peso tradicional"
    
    # Búsqueda híbrida con más peso vectorial
    make_request "$API_BASE/hybrid?query=impuestos%20sobre%20la%20renta&limit=5&traditionalWeight=0.3&vectorWeight=0.7" "Búsqueda híbrida con más peso vectorial"
    
    echo "=========================================="
    print_status "🧠 PRUEBAS DE BÚSQUEDA INTELIGENTE"
    echo "=========================================="
    
    # Búsqueda híbrida inteligente
    make_request "$API_BASE/smart-hybrid?query=IVA&limit=5" "Búsqueda híbrida inteligente - IVA"
    
    # Búsqueda híbrida inteligente con consulta larga
    make_request "$API_BASE/smart-hybrid?query=impuestos%20sobre%20la%20renta%20de%20las%20personas%20naturales&limit=3" "Búsqueda híbrida inteligente con consulta larga"
    
    echo "=========================================="
    print_status "📈 COMPARACIÓN CON BÚSQUEDA TRADICIONAL"
    echo "=========================================="
    
    # Comparar con búsqueda tradicional
    make_request "$API_BASE/documents?query=IVA&limit=5" "Búsqueda tradicional - IVA"
    make_request "$API_BASE/hybrid?query=IVA&limit=5" "Búsqueda híbrida - IVA"
    
    echo "=========================================="
    print_status "✅ PRUEBAS AUTOMÁTICAS COMPLETADAS"
    echo "=========================================="
}

# Función para mostrar ejemplos de uso en Swagger
show_swagger_examples() {
    print_swagger "📝 Ejemplos de consultas para probar en Swagger UI:"
    echo ""
    
    echo "🔍 Búsquedas básicas:"
    echo "  - IVA"
    echo "  - impuestos"
    echo "  - renta"
    echo "  - contribuyente"
    echo ""
    
    echo "🧠 Búsquedas semánticas (para probar búsqueda vectorial):"
    echo "  - impuestos sobre la renta"
    echo "  - declaración de impuestos"
    echo "  - obligaciones tributarias"
    echo "  - régimen fiscal"
    echo "  - exenciones tributarias"
    echo ""
    
    echo "⚖️ Configuraciones de pesos para probar:"
    echo "  - traditionalWeight=0.8, vectorWeight=0.2 (más tradicional)"
    echo "  - traditionalWeight=0.5, vectorWeight=0.5 (balanceado)"
    echo "  - traditionalWeight=0.2, vectorWeight=0.8 (más vectorial)"
    echo ""
    
    echo "📊 Límites de resultados:"
    echo "  - limit=3 (pocos resultados)"
    echo "  - limit=10 (resultados estándar)"
    echo "  - limit=20 (muchos resultados)"
    echo ""
}

# Función para abrir Swagger en el navegador
open_swagger() {
    print_swagger "🌐 Abriendo Swagger UI en el navegador..."
    
    if command -v open &> /dev/null; then
        # macOS
        open "$SWAGGER_URL"
    elif command -v xdg-open &> /dev/null; then
        # Linux
        xdg-open "$SWAGGER_URL"
    elif command -v start &> /dev/null; then
        # Windows
        start "$SWAGGER_URL"
    else
        print_warning "No se pudo abrir automáticamente el navegador"
        print_status "Abre manualmente: $SWAGGER_URL"
    fi
}

# Función para mostrar guía de uso de Swagger
show_swagger_guide() {
    print_swagger "📖 Guía de uso de Swagger UI para búsqueda híbrida:"
    echo ""
    echo "1. 🌐 Accede a Swagger UI: $SWAGGER_URL"
    echo ""
    echo "2. 📋 Busca la sección 'Búsqueda de Documentos'"
    echo ""
    echo "3. 🔍 Encuentra los endpoints híbridos:"
    echo "   - GET /api/search/hybrid-stats"
    echo "   - GET /api/search/hybrid"
    echo "   - GET /api/search/smart-hybrid"
    echo ""
    echo "4. 🧪 Para probar cada endpoint:"
    echo "   a) Haz clic en el endpoint"
    echo "   b) Haz clic en 'Try it out'"
    echo "   c) Completa los parámetros"
    echo "   d) Haz clic en 'Execute'"
    echo ""
    echo "5. 📊 Interpreta los resultados:"
    echo "   - traditionalScore: Score de búsqueda tradicional"
    echo "   - vectorScore: Score de búsqueda vectorial"
    echo "   - hybridScore: Score combinado"
    echo "   - matchType: Tipo de coincidencia (traditional/vector/hybrid)"
    echo ""
}

# Función de ayuda
show_help() {
    echo "🚀 Script de Pruebas de Búsqueda Híbrida en Swagger"
    echo ""
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help           Mostrar esta ayuda"
    echo "  --auto               Ejecutar solo pruebas automáticas"
    echo "  --swagger-info       Mostrar solo información de Swagger"
    echo "  --examples           Mostrar solo ejemplos de consultas"
    echo "  --guide              Mostrar guía de uso de Swagger"
    echo "  --open               Abrir Swagger UI en el navegador"
    echo "  --url URL            URL base del microservicio (default: http://localhost:8080)"
    echo ""
    echo "Ejemplos:"
    echo "  $0                           # Mostrar información completa"
    echo "  $0 --auto                    # Ejecutar pruebas automáticas"
    echo "  $0 --open                    # Abrir Swagger UI"
    echo "  $0 --examples                # Mostrar ejemplos de consultas"
    echo ""
}

# Función principal
main() {
    local auto_only=false
    local swagger_info_only=false
    local examples_only=false
    local guide_only=false
    local open_only=false
    
    # Procesar argumentos
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            --auto)
                auto_only=true
                shift
                ;;
            --swagger-info)
                swagger_info_only=true
                shift
                ;;
            --examples)
                examples_only=true
                shift
                ;;
            --guide)
                guide_only=true
                shift
                ;;
            --open)
                open_only=true
                shift
                ;;
            --url)
                BASE_URL="$2"
                SWAGGER_URL="$BASE_URL/swagger-ui.html"
                API_BASE="$BASE_URL/api/search"
                shift 2
                ;;
            *)
                print_error "Argumento no reconocido: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    echo "🚀 Pruebas de Búsqueda Híbrida en Swagger"
    echo "========================================"
    echo ""
    
    # Ejecutar según la opción seleccionada
    if [ "$auto_only" = true ]; then
        if ! check_service; then
            exit 1
        fi
        run_automatic_tests
    elif [ "$swagger_info_only" = true ]; then
        show_swagger_info
    elif [ "$examples_only" = true ]; then
        show_swagger_examples
    elif [ "$guide_only" = true ]; then
        show_swagger_guide
    elif [ "$open_only" = true ]; then
        open_swagger
    else
        # Mostrar información completa
        if ! check_service; then
            exit 1
        fi
        
        show_swagger_info
        echo ""
        show_swagger_examples
        echo ""
        show_swagger_guide
        echo ""
        
        print_status "¿Deseas ejecutar pruebas automáticas? (y/n)"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            echo ""
            run_automatic_tests
        fi
        
        echo ""
        print_status "¿Deseas abrir Swagger UI en el navegador? (y/n)"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            open_swagger
        fi
    fi
    
    print_success "🎉 Script de pruebas de Swagger completado"
}

# Ejecutar función principal
main "$@"
