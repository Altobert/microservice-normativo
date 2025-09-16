#!/bin/bash

# 🚀 Script de Pruebas para Búsqueda Híbrida
# Este script prueba los nuevos endpoints de búsqueda híbrida (tradicional + vectorial)

set -e

# Configuración
BASE_URL="http://localhost:8080"
API_BASE="$BASE_URL/api/search"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# Función para hacer requests HTTP
make_request() {
    local endpoint="$1"
    local description="$2"
    local expected_status="${3:-200}"
    
    print_status "Probando: $description"
    print_status "Endpoint: $endpoint"
    
    response=$(curl -s -w "\n%{http_code}" "$endpoint" || echo "000")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
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
        print_warning "Ejecuta: ./start-app.sh"
        return 1
    fi
}

# Función principal de pruebas
run_tests() {
    print_status "🚀 Iniciando pruebas de búsqueda híbrida..."
    echo ""
    
    # Verificar servicio
    if ! check_service; then
        exit 1
    fi
    
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
    make_request "$API_BASE/hybrid?query=impuestos&limit=3&traditionalWeight=0.7&vectorWeight=0.3" "Búsqueda híbrida con pesos personalizados"
    
    # Búsqueda híbrida con más peso vectorial
    make_request "$API_BASE/hybrid?query=impuestos%20sobre%20la%20renta&limit=5&traditionalWeight=0.3&vectorWeight=0.7" "Búsqueda híbrida con más peso vectorial"
    
    # Búsqueda híbrida con consulta larga
    make_request "$API_BASE/hybrid?query=impuestos%20sobre%20la%20renta%20de%20las%20personas%20naturales&limit=3" "Búsqueda híbrida con consulta larga"
    
    echo "=========================================="
    print_status "🧠 PRUEBAS DE BÚSQUEDA INTELIGENTE"
    echo "=========================================="
    
    # Búsqueda híbrida inteligente
    make_request "$API_BASE/smart-hybrid?query=IVA&limit=5" "Búsqueda híbrida inteligente - IVA"
    
    # Búsqueda híbrida inteligente con consulta larga
    make_request "$API_BASE/smart-hybrid?query=impuestos%20sobre%20la%20renta%20de%20las%20personas%20naturales&limit=3" "Búsqueda híbrida inteligente con consulta larga"
    
    # Búsqueda híbrida inteligente con palabras cortas
    make_request "$API_BASE/smart-hybrid?query=el%20la%20de%20en&limit=5" "Búsqueda híbrida inteligente con palabras cortas"
    
    echo "=========================================="
    print_status "🔬 PRUEBAS DE CASOS LÍMITE"
    echo "=========================================="
    
    # Consulta vacía
    make_request "$API_BASE/hybrid?query=&limit=5" "Búsqueda híbrida con consulta vacía" "500"
    
    # Límite muy alto
    make_request "$API_BASE/hybrid?query=impuestos&limit=1000" "Búsqueda híbrida con límite muy alto"
    
    # Pesos inválidos
    make_request "$API_BASE/hybrid?query=impuestos&limit=5&traditionalWeight=1.5&vectorWeight=0.5" "Búsqueda híbrida con pesos inválidos"
    
    echo "=========================================="
    print_status "📈 COMPARACIÓN CON BÚSQUEDA TRADICIONAL"
    echo "=========================================="
    
    # Comparar con búsqueda tradicional
    make_request "$API_BASE/documents?query=IVA&limit=5" "Búsqueda tradicional - IVA"
    make_request "$API_BASE/hybrid?query=IVA&limit=5" "Búsqueda híbrida - IVA"
    
    echo "=========================================="
    print_status "✅ PRUEBAS COMPLETADAS"
    echo "=========================================="
    
    print_success "Todas las pruebas de búsqueda híbrida han sido ejecutadas"
    print_status "Revisa los resultados arriba para verificar el funcionamiento"
}

# Función de ayuda
show_help() {
    echo "🚀 Script de Pruebas para Búsqueda Híbrida"
    echo ""
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help     Mostrar esta ayuda"
    echo "  -v, --verbose  Modo verbose (mostrar más detalles)"
    echo "  --url URL      URL base del microservicio (default: http://localhost:8080)"
    echo ""
    echo "Ejemplos:"
    echo "  $0                    # Ejecutar todas las pruebas"
    echo "  $0 --url http://localhost:9090  # Usar puerto diferente"
    echo ""
}

# Función para ejecutar pruebas específicas
run_specific_test() {
    local test_type="$1"
    
    case "$test_type" in
        "stats")
            print_status "Ejecutando solo pruebas de estadísticas..."
            make_request "$API_BASE/hybrid-stats" "Estadísticas del sistema híbrido"
            ;;
        "hybrid")
            print_status "Ejecutando solo pruebas de búsqueda híbrida..."
            make_request "$API_BASE/hybrid?query=IVA&limit=5" "Búsqueda híbrida básica"
            ;;
        "smart")
            print_status "Ejecutando solo pruebas de búsqueda inteligente..."
            make_request "$API_BASE/smart-hybrid?query=IVA&limit=5" "Búsqueda híbrida inteligente"
            ;;
        *)
            print_error "Tipo de prueba no reconocido: $test_type"
            print_status "Tipos disponibles: stats, hybrid, smart"
            exit 1
            ;;
    esac
}

# Procesar argumentos de línea de comandos
VERBOSE=false
SPECIFIC_TEST=""

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
        --url)
            BASE_URL="$2"
            API_BASE="$BASE_URL/api/search"
            shift 2
            ;;
        --test)
            SPECIFIC_TEST="$2"
            shift 2
            ;;
        *)
            print_error "Argumento no reconocido: $1"
            show_help
            exit 1
            ;;
    esac
done

# Configurar modo verbose
if [ "$VERBOSE" = true ]; then
    set -x
fi

# Ejecutar pruebas específicas o todas
if [ -n "$SPECIFIC_TEST" ]; then
    run_specific_test "$SPECIFIC_TEST"
else
    run_tests
fi

print_success "🎉 Script de pruebas de búsqueda híbrida completado"
