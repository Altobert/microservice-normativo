#!/bin/bash

# 🚀 Script de Inicio para Microservicio con Búsqueda Híbrida
# Este script compila e inicia el microservicio con las nuevas capacidades de búsqueda vectorial

set -e

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

# Función para verificar dependencias
check_dependencies() {
    print_status "Verificando dependencias del sistema..."
    
    # Verificar Java
    if ! command -v java &> /dev/null; then
        print_error "Java no está instalado"
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ]; then
        print_error "Se requiere Java 17 o superior. Versión actual: $java_version"
        exit 1
    fi
    
    print_success "✓ Java $java_version detectado"
    
    # Verificar Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven no está instalado"
        exit 1
    fi
    
    print_success "✓ Maven detectado"
    
    # Verificar curl (para tests)
    if ! command -v curl &> /dev/null; then
        print_warning "curl no está instalado - algunos tests pueden fallar"
    else
        print_success "✓ curl detectado"
    fi
    
    # Verificar jq (para tests)
    if ! command -v jq &> /dev/null; then
        print_warning "jq no está instalado - algunos tests pueden fallar"
    else
        print_success "✓ jq detectado"
    fi
}

# Función para compilar el proyecto
compile_project() {
    print_status "Compilando proyecto con nuevas dependencias de búsqueda vectorial..."
    
    # Limpiar compilaciones anteriores
    print_status "Limpiando compilaciones anteriores..."
    mvn clean -q
    
    # Compilar con las nuevas dependencias
    print_status "Compilando con dependencias de Lucene Vector y Spring AI..."
    if mvn compile -q; then
        print_success "✓ Compilación exitosa"
    else
        print_error "✗ Error en la compilación"
        print_status "Verificando dependencias en pom.xml..."
        exit 1
    fi
}

# Función para verificar configuración
check_configuration() {
    print_status "Verificando configuración del microservicio..."
    
    # Verificar application.properties
    if [ -f "src/main/resources/application.properties" ]; then
        print_success "✓ application.properties encontrado"
        
        # Verificar configuración de índice
        if grep -q "lucene.index.directory" src/main/resources/application.properties; then
            index_dir=$(grep "lucene.index.directory" src/main/resources/application.properties | cut -d'=' -f2)
            print_status "Directorio de índice configurado: $index_dir"
            
            if [ -d "$index_dir" ]; then
                print_success "✓ Directorio de índice existe"
            else
                print_warning "⚠ Directorio de índice no existe: $index_dir"
                print_status "El microservicio creará el índice automáticamente"
            fi
        else
            print_warning "⚠ Configuración de índice no encontrada"
        fi
    else
        print_error "✗ application.properties no encontrado"
        exit 1
    fi
}

# Función para iniciar el microservicio
start_microservice() {
    print_status "Iniciando microservicio con búsqueda híbrida..."
    
    # Verificar si ya está ejecutándose
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_warning "⚠ Microservicio ya está ejecutándose en puerto 8080"
        print_status "Deteniendo proceso anterior..."
        pkill -f "spring-boot:run" || true
        sleep 2
    fi
    
    print_status "Iniciando microservicio en segundo plano..."
    nohup mvn spring-boot:run -Dmaven.test.skip=true > app.log 2>&1 &
    
    # Esperar a que el servicio esté listo
    print_status "Esperando a que el microservicio esté listo..."
    for i in {1..30}; do
        if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            print_success "✓ Microservicio iniciado exitosamente"
            break
        fi
        
        if [ $i -eq 30 ]; then
            print_error "✗ Timeout esperando que el microservicio inicie"
            print_status "Revisa app.log para más detalles"
            exit 1
        fi
        
        sleep 2
        print_status "Esperando... ($i/30)"
    done
}

# Función para verificar funcionalidades
verify_functionality() {
    print_status "Verificando funcionalidades del microservicio..."
    
    # Verificar health check
    if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
        print_success "✓ Health check OK"
    else
        print_error "✗ Health check falló"
        return 1
    fi
    
    # Verificar Swagger UI
    if curl -s http://localhost:8080/swagger-ui.html > /dev/null 2>&1; then
        print_success "✓ Swagger UI disponible"
    else
        print_warning "⚠ Swagger UI no disponible"
    fi
    
    # Verificar endpoints de búsqueda tradicional
    if curl -s "http://localhost:8080/api/search/stats" > /dev/null 2>&1; then
        print_success "✓ Endpoints de búsqueda tradicional funcionando"
    else
        print_warning "⚠ Endpoints de búsqueda tradicional no disponibles"
    fi
    
    # Verificar endpoints de búsqueda híbrida
    if curl -s "http://localhost:8080/api/search/hybrid-stats" > /dev/null 2>&1; then
        print_success "✓ Endpoints de búsqueda híbrida funcionando"
    else
        print_warning "⚠ Endpoints de búsqueda híbrida no disponibles"
    fi
}

# Función para mostrar información del sistema
show_system_info() {
    print_status "Información del sistema de búsqueda híbrida:"
    echo ""
    echo "🔗 URLs disponibles:"
    echo "  - Health Check: http://localhost:8080/actuator/health"
    echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "  - API Docs: http://localhost:8080/api-docs"
    echo ""
    echo "🚀 Endpoints de búsqueda híbrida:"
    echo "  - Búsqueda híbrida: http://localhost:8080/api/search/hybrid?query=impuestos&limit=10"
    echo "  - Búsqueda inteligente: http://localhost:8080/api/search/smart-hybrid?query=impuestos sobre la renta&limit=10"
    echo "  - Estadísticas híbridas: http://localhost:8080/api/search/hybrid-stats"
    echo ""
    echo "🧪 Scripts de prueba:"
    echo "  - ./test-hybrid-search.sh          # Probar búsqueda híbrida"
    echo "  - ./test-microservice.sh           # Probar microservicio completo"
    echo "  - ./benchmark-search-system.sh     # Benchmark del sistema"
    echo ""
    echo "📊 Logs del microservicio:"
    echo "  - tail -f app.log                  # Ver logs en tiempo real"
    echo ""
}

# Función de ayuda
show_help() {
    echo "🚀 Script de Inicio para Microservicio con Búsqueda Híbrida"
    echo ""
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help           Mostrar esta ayuda"
    echo "  --skip-compile       Saltar compilación (usar si ya está compilado)"
    echo "  --skip-verify        Saltar verificación de funcionalidades"
    echo "  --port PORT          Puerto personalizado (default: 8080)"
    echo ""
    echo "Ejemplos:"
    echo "  $0                           # Inicio completo"
    echo "  $0 --skip-compile            # Solo iniciar (sin compilar)"
    echo "  $0 --port 9090               # Usar puerto 9090"
    echo ""
}

# Función principal
main() {
    local skip_compile=false
    local skip_verify=false
    local port=8080
    
    # Procesar argumentos
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            --skip-compile)
                skip_compile=true
                shift
                ;;
            --skip-verify)
                skip_verify=true
                shift
                ;;
            --port)
                port="$2"
                shift 2
                ;;
            *)
                print_error "Argumento no reconocido: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    echo "🚀 Iniciando Microservicio con Búsqueda Híbrida"
    echo "=============================================="
    echo ""
    
    # Verificar dependencias
    check_dependencies
    echo ""
    
    # Compilar proyecto (si no se salta)
    if [ "$skip_compile" = false ]; then
        compile_project
        echo ""
    else
        print_status "Saltando compilación..."
        echo ""
    fi
    
    # Verificar configuración
    check_configuration
    echo ""
    
    # Iniciar microservicio
    start_microservice
    echo ""
    
    # Verificar funcionalidades (si no se salta)
    if [ "$skip_verify" = false ]; then
        verify_functionality
        echo ""
    else
        print_status "Saltando verificación de funcionalidades..."
        echo ""
    fi
    
    # Mostrar información del sistema
    show_system_info
    
    print_success "🎉 Microservicio con búsqueda híbrida iniciado exitosamente!"
    print_status "Presiona Ctrl+C para detener el servicio"
    
    # Mantener el script ejecutándose para mostrar logs
    tail -f app.log
}

# Ejecutar función principal
main "$@"
