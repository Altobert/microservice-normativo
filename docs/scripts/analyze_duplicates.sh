#!/bin/bash

echo "🔍 ANALIZADOR DE DUPLICADOS EN DOCUMENTOS"
echo "========================================"

DOCS_DIR="/Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII"

echo ""
echo "📊 1. ANÁLISIS DE ARCHIVOS EN EL DIRECTORIO"
echo "------------------------------------------"

# Contar archivos PDF
TOTAL_PDFS=$(find "$DOCS_DIR" -name "*.pdf" | wc -l)
echo "📄 Total archivos PDF: $TOTAL_PDFS"

# Buscar archivos duplicados por nombre
echo ""
echo "🔍 Buscando archivos con nombres similares..."
DUPLICATE_NAMES=$(find "$DOCS_DIR" -name "*.pdf" -exec basename {} \; | sort | uniq -d)
if [ -n "$DUPLICATE_NAMES" ]; then
    echo "⚠️  Archivos con nombres duplicados encontrados:"
    echo "$DUPLICATE_NAMES" | while read name; do
        echo "   📄 $name"
        find "$DOCS_DIR" -name "$name" -exec echo "      📁 {}" \;
    done
else
    echo "✅ No se encontraron archivos con nombres duplicados"
fi

# Buscar archivos duplicados por tamaño
echo ""
echo "🔍 Buscando archivos con tamaños similares..."
find "$DOCS_DIR" -name "*.pdf" -exec ls -la {} \; | awk '{print $5, $9}' | sort -n | uniq -d -f1 | while read size file; do
    echo "   📄 Tamaño: $size bytes - Archivo: $file"
done

echo ""
echo "📊 2. ANÁLISIS DEL ÍNDICE LUCENE"
echo "-------------------------------"

# Verificar estadísticas del índice
STATS=$(curl -s "http://localhost:8080/api/search/stats" 2>/dev/null)
if [ $? -eq 0 ]; then
    INDEXED_DOCS=$(echo "$STATS" | jq -r '.totalDocuments // "N/A"')
    echo "📄 Documentos en el índice: $INDEXED_DOCS"
    
    # Calcular ratio
    if [ "$INDEXED_DOCS" != "N/A" ] && [ "$TOTAL_PDFS" -gt 0 ]; then
        RATIO=$(echo "scale=2; $INDEXED_DOCS / $TOTAL_PDFS" | bc -l)
        echo "📊 Ratio índice/archivos: $RATIO"
        
        if (( $(echo "$RATIO > 1.5" | bc -l) )); then
            echo "⚠️  PROBLEMA: Demasiados documentos indexados vs archivos"
        elif (( $(echo "$RATIO < 0.5" | bc -l) )); then
            echo "⚠️  PROBLEMA: Pocos documentos indexados vs archivos"
        else
            echo "✅ Ratio parece normal"
        fi
    fi
else
    echo "❌ No se pudo obtener estadísticas del índice"
fi

echo ""
echo "🔍 3. PRUEBA DE BÚSQUEDA DETALLADA"
echo "--------------------------------"

# Probar varias búsquedas para detectar duplicados
QUERIES=("IVA" "impuestos" "tributario" "contribuyente")
for query in "${QUERIES[@]}"; do
    echo ""
    echo "🔍 Búsqueda: '$query'"
    SEARCH_RESULT=$(curl -s "http://localhost:8080/api/search/documents?query=$query&limit=10")
    
    if [ $? -eq 0 ]; then
        TOTAL_RESULTS=$(echo "$SEARCH_RESULT" | jq -r '.totalResults // 0')
        UNIQUE_FILES=$(echo "$SEARCH_RESULT" | jq -r '.results[] | .filename' | sort | uniq | wc -l)
        ACTUAL_RESULTS=$(echo "$SEARCH_RESULT" | jq -r '.results | length')
        
        echo "   📄 Total resultados: $TOTAL_RESULTS"
        echo "   📄 Resultados únicos: $UNIQUE_FILES"
        echo "   📄 Resultados actuales: $ACTUAL_RESULTS"
        
        if [ "$UNIQUE_FILES" -lt "$ACTUAL_RESULTS" ]; then
            echo "   ⚠️  DUPLICADOS DETECTADOS"
            echo "$SEARCH_RESULT" | jq -r '.results[] | .filename' | sort | uniq -c | sort -nr | head -3
        else
            echo "   ✅ Sin duplicados detectados"
        fi
    else
        echo "   ❌ Error en la búsqueda"
    fi
done

echo ""
echo "📋 4. RECOMENDACIONES"
echo "-------------------"

# Analizar si hay problemas
PROBLEMS_DETECTED=false

# Verificar si hay demasiados documentos indexados
if [ "$INDEXED_DOCS" != "N/A" ] && [ "$INDEXED_DOCS" -gt $((TOTAL_PDFS * 2)) ]; then
    echo "⚠️  PROBLEMA: Demasiados documentos indexados ($INDEXED_DOCS vs $TOTAL_PDFS archivos)"
    PROBLEMS_DETECTED=true
fi

# Verificar duplicados en búsquedas
if [ "$UNIQUE_FILES" -lt "$ACTUAL_RESULTS" ]; then
    echo "⚠️  PROBLEMA: Duplicados detectados en búsquedas"
    PROBLEMS_DETECTED=true
fi

if [ "$PROBLEMS_DETECTED" = true ]; then
    echo ""
    echo "🔧 SOLUCIONES RECOMENDADAS:"
    echo "   1. Limpiar el índice completamente: ./clean_lucene_index.sh"
    echo "   2. Verificar archivos duplicados en el directorio fuente"
    echo "   3. Reindexar con configuración de deduplicación"
    echo "   4. Implementar validación de duplicados en el indexador"
else
    echo "✅ No se detectaron problemas significativos"
fi

echo ""
echo "🎯 5. COMANDOS ÚTILES"
echo "-------------------"
echo "📊 Ver estadísticas: curl http://localhost:8080/api/search/stats"
echo "🔍 Probar búsqueda: curl 'http://localhost:8080/api/search/documents?query=IVA&limit=5'"
echo "🧹 Limpiar índice: ./clean_lucene_index.sh"
echo "📁 Ver archivos: find $DOCS_DIR -name '*.pdf' | wc -l"
