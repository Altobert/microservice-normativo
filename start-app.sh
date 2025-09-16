#!/bin/bash

# Script para levantar el Microservicio de Búsqueda de Documentos Normativos SII
# Autor: Sistema de Gestión de Documentos Normativos
# Fecha: $(date +%Y-%m-%d)

echo "🚀 Iniciando Microservicio de Búsqueda de Documentos Normativos SII..."
echo "📁 Directorio del proyecto: $(pwd)"
echo "📅 Fecha: $(date)"
echo ""

# Verificar que estamos en el directorio correcto
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: No se encontró el archivo pom.xml"
    echo "   Asegúrate de ejecutar este script desde el directorio raíz del proyecto"
    exit 1
fi

# Verificar que Java está instalado
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java no está instalado o no está en el PATH"
    echo "   Por favor instala Java 17 o superior"
    exit 1
fi

# Verificar que Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven no está instalado o no está en el PATH"
    echo "   Por favor instala Maven"
    exit 1
fi

# Mostrar información del entorno
echo "🔍 Información del entorno:"
echo "   Java version: $(java -version 2>&1 | head -n 1)"
echo "   Maven version: $(mvn -version | head -n 1)"
echo ""

# Verificar que el índice existe
INDEX_DIR="/Users/albertosanmartin/usach-memoria-implementacion/desarrollo/proyecto-normativo-ms/pipelinenormativosii/lucene-index"
if [ ! -d "$INDEX_DIR" ]; then
    echo "⚠️  Advertencia: El directorio del índice no existe: $INDEX_DIR"
    echo "   La aplicación podría no funcionar correctamente"
    echo ""
fi

# Compilar el proyecto
echo "🔨 Compilando el proyecto..."
if mvn clean compile -q; then
    echo "✅ Compilación exitosa"
else
    echo "❌ Error en la compilación"
    exit 1
fi

echo ""
echo "🌐 Iniciando aplicación en puerto 8080..."
echo "📖 Documentación disponible en: http://localhost:8080/swagger-ui.html"
echo "🔍 API de búsqueda disponible en: http://localhost:8080/api/search/documents"
echo "📊 Estadísticas del índice en: http://localhost:8080/api/search/stats"
echo ""
echo "💡 Para detener la aplicación, presiona Ctrl+C o ejecuta: ./stop-app.sh"
echo ""

# Ejecutar la aplicación
mvn spring-boot:run
