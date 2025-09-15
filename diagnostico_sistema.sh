#!/bin/bash

echo "🔍 DIAGNÓSTICO DEL SISTEMA DE INDEXACIÓN"
echo "========================================"

echo ""
echo "📊 1. ESTADO DEL MICROSERVICIO"
echo "-----------------------------"
if pgrep -f "LoadnormasApplication" > /dev/null; then
    echo "✅ Microservicio ejecutándose"
    echo "   PID: $(pgrep -f "LoadnormasApplication")"
else
    echo "❌ Microservicio NO ejecutándose"
fi

echo ""
echo "📁 2. DIRECTORIO DE DOCUMENTOS"
echo "-----------------------------"
DOCS_DIR="/Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII"
if [ -d "$DOCS_DIR" ]; then
    echo "✅ Directorio existe: $DOCS_DIR"
    PDF_COUNT=$(find "$DOCS_DIR" -name "*.pdf" | wc -l)
    echo "   📄 Archivos PDF encontrados: $PDF_COUNT"
else
    echo "❌ Directorio NO existe: $DOCS_DIR"
fi

echo ""
echo "🗂️ 3. DIRECTORIO DE ÍNDICE"
echo "-------------------------"
INDEX_DIR="path/to/index"
if [ -d "$INDEX_DIR" ]; then
    echo "✅ Directorio de índice existe: $INDEX_DIR"
    INDEX_FILES=$(ls -la "$INDEX_DIR" | wc -l)
    echo "   📁 Archivos en índice: $((INDEX_FILES - 1))"
else
    echo "❌ Directorio de índice NO existe: $INDEX_DIR"
fi

echo ""
echo "🌐 4. CONECTIVIDAD API"
echo "--------------------"
if curl -s "http://localhost:8080/api/search/stats" > /dev/null 2>&1; then
    echo "✅ API respondiendo correctamente"
    STATS=$(curl -s "http://localhost:8080/api/search/stats" | jq -r '.totalDocuments // "N/A"')
    echo "   📊 Total documentos indexados: $STATS"
else
    echo "❌ API NO respondiendo"
fi

echo ""
echo "🔧 5. PERMISOS DE ARCHIVOS"
echo "-------------------------"
if [ -r "$DOCS_DIR" ]; then
    echo "✅ Permisos de lectura en directorio de documentos"
else
    echo "❌ Sin permisos de lectura en directorio de documentos"
fi

if [ -w "$INDEX_DIR" ]; then
    echo "✅ Permisos de escritura en directorio de índice"
else
    echo "❌ Sin permisos de escritura en directorio de índice"
fi

echo ""
echo "💾 6. ESPACIO EN DISCO"
echo "---------------------"
df -h . | tail -1 | awk '{print "   💿 Espacio disponible: " $4 " de " $2 " (" $5 " usado)"}'

echo ""
echo "📋 7. LOGS RECIENTES"
echo "-------------------"
echo "   Últimas líneas del log del microservicio:"
if pgrep -f "LoadnormasApplication" > /dev/null; then
    echo "   (El microservicio está ejecutándose - revisa los logs en la consola)"
else
    echo "   (El microservicio no está ejecutándose)"
fi

echo ""
echo "🎯 8. RECOMENDACIONES"
echo "-------------------"
if ! pgrep -f "LoadnormasApplication" > /dev/null; then
    echo "   ⚠️  Inicia el microservicio: mvn spring-boot:run"
fi

if [ ! -d "$DOCS_DIR" ]; then
    echo "   ⚠️  Verifica la ruta del directorio de documentos"
fi

if [ ! -d "$INDEX_DIR" ]; then
    echo "   ⚠️  Crea el directorio de índice: mkdir -p $INDEX_DIR"
fi

echo ""
echo "✅ Diagnóstico completado"
echo "========================="
