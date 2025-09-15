#!/bin/bash

echo "🔍 MONITOR DE ERRORES - SISTEMA DE INDEXACIÓN"
echo "============================================="

# Función para verificar el estado del microservicio
check_microservice() {
    if pgrep -f "LoadnormasApplication" > /dev/null; then
        echo "✅ $(date): Microservicio ejecutándose"
        return 0
    else
        echo "❌ $(date): Microservicio NO ejecutándose"
        return 1
    fi
}

# Función para verificar la API
check_api() {
    if curl -s "http://localhost:8080/api/search/stats" > /dev/null 2>&1; then
        echo "✅ $(date): API respondiendo"
        return 0
    else
        echo "❌ $(date): API NO respondiendo"
        return 1
    fi
}

# Función para verificar el índice
check_index() {
    INDEX_DIR="path/to/index"
    if [ -d "$INDEX_DIR" ] && [ -r "$INDEX_DIR" ]; then
        echo "✅ $(date): Índice accesible"
        return 0
    else
        echo "❌ $(date): Índice NO accesible"
        return 1
    fi
}

# Función para verificar documentos
check_documents() {
    DOCS_DIR="/Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII"
    if [ -d "$DOCS_DIR" ] && [ -r "$DOCS_DIR" ]; then
        PDF_COUNT=$(find "$DOCS_DIR" -name "*.pdf" 2>/dev/null | wc -l)
        echo "✅ $(date): $PDF_COUNT documentos disponibles"
        return 0
    else
        echo "❌ $(date): Directorio de documentos NO accesible"
        return 1
    fi
}

# Función para realizar una búsqueda de prueba
test_search() {
    if curl -s "http://localhost:8080/api/search/documents?query=test&limit=1" > /dev/null 2>&1; then
        echo "✅ $(date): Búsqueda funcionando"
        return 0
    else
        echo "❌ $(date): Error en búsqueda"
        return 1
    fi
}

echo "Iniciando monitoreo cada 30 segundos..."
echo "Presiona Ctrl+C para detener"
echo ""

while true; do
    echo "--- Verificación $(date) ---"
    
    # Verificar todos los componentes
    check_microservice
    check_api
    check_index
    check_documents
    test_search
    
    echo ""
    sleep 30
done
