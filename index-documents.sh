#!/bin/bash

# Script para indexar documentos SII
# Uso: ./index-documents.sh [directorio_opcional]

echo "🚀 Iniciando indexación de documentos SII..."

# Directorio por defecto
DEFAULT_DIR="/Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII"

# Usar directorio proporcionado o el por defecto
DIRECTORY=${1:-$DEFAULT_DIR}

echo "📁 Directorio a indexar: $DIRECTORY"

# Verificar que el directorio existe
if [ ! -d "$DIRECTORY" ]; then
    echo "❌ Error: El directorio $DIRECTORY no existe"
    exit 1
fi

# Ejecutar la indexación usando Maven
echo "🔧 Ejecutando indexación..."
mvn exec:java -Dexec.mainClass="cl.sii.normativo.loadnormas.IndexDocumentsRunner" -Dexec.args="$DIRECTORY" -Dmaven.test.skip=true

echo "✅ Indexación completada"
