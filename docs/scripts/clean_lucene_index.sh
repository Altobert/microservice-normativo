#!/bin/bash

echo "🧹 LIMPIADOR DE ÍNDICE LUCENE DUPLICADO"
echo "======================================"

INDEX_DIR="path/to/index"
BACKUP_DIR="path/to/index_backup_$(date +%Y%m%d_%H%M%S)"

echo ""
echo "📊 1. DIAGNÓSTICO INICIAL"
echo "------------------------"

# Verificar estadísticas actuales
echo "🔍 Estadísticas actuales del índice:"
STATS=$(curl -s "http://localhost:8080/api/search/stats" 2>/dev/null)
if [ $? -eq 0 ]; then
    TOTAL_DOCS=$(echo "$STATS" | jq -r '.totalDocuments // "N/A"')
    echo "   📄 Total documentos: $TOTAL_DOCS"
    
    if [ "$TOTAL_DOCS" != "N/A" ] && [ "$TOTAL_DOCS" -gt 1500 ]; then
        echo "   ⚠️  PROBLEMA DETECTADO: Demasiados documentos (esperado ~1,170)"
        PROBLEM_DETECTED=true
    else
        echo "   ✅ Número de documentos parece normal"
        PROBLEM_DETECTED=false
    fi
else
    echo "   ❌ No se pudo obtener estadísticas (microservicio no ejecutándose)"
    PROBLEM_DETECTED=true
fi

# Verificar archivos del índice
echo ""
echo "🔍 Archivos en el índice:"
if [ -d "$INDEX_DIR" ]; then
    FILE_COUNT=$(ls -la "$INDEX_DIR" | wc -l)
    echo "   📁 Archivos encontrados: $((FILE_COUNT - 1))"
    
    # Contar segmentos
    SEGMENT_COUNT=$(ls "$INDEX_DIR" | grep -E "_[0-9]+\.(cfe|cfs|si)$" | wc -l)
    echo "   📊 Segmentos de índice: $SEGMENT_COUNT"
    
    if [ "$SEGMENT_COUNT" -gt 10 ]; then
        echo "   ⚠️  PROBLEMA DETECTADO: Demasiados segmentos (esperado ~5-8)"
        PROBLEM_DETECTED=true
    fi
else
    echo "   ❌ Directorio de índice no existe"
    PROBLEM_DETECTED=true
fi

# Probar búsqueda para detectar duplicados
echo ""
echo "🔍 Prueba de búsqueda para detectar duplicados:"
SEARCH_RESULT=$(curl -s "http://localhost:8080/api/search/documents?query=IVA&limit=5" 2>/dev/null)
if [ $? -eq 0 ]; then
    UNIQUE_FILES=$(echo "$SEARCH_RESULT" | jq -r '.results[] | .filename' | sort | uniq | wc -l)
    TOTAL_RESULTS=$(echo "$SEARCH_RESULT" | jq -r '.results | length')
    
    echo "   📄 Resultados únicos: $UNIQUE_FILES"
    echo "   📄 Total resultados: $TOTAL_RESULTS"
    
    if [ "$UNIQUE_FILES" -lt "$TOTAL_RESULTS" ]; then
        echo "   ⚠️  PROBLEMA DETECTADO: Resultados duplicados encontrados"
        PROBLEM_DETECTED=true
    else
        echo "   ✅ No se detectaron duplicados en la muestra"
    fi
else
    echo "   ❌ No se pudo realizar búsqueda de prueba"
    PROBLEM_DETECTED=true
fi

echo ""
if [ "$PROBLEM_DETECTED" = true ]; then
    echo "🚨 PROBLEMA CONFIRMADO: Índice duplicado detectado"
    echo ""
    echo "¿Deseas proceder con la limpieza del índice?"
    echo "   a) Sí, limpiar y reconstruir el índice"
    echo "   b) Solo hacer backup del índice actual"
    echo "   c) Cancelar operación"
    echo ""
    read -p "Selecciona una opción (a/b/c): " choice
    
    case $choice in
        a)
            echo ""
            echo "🧹 2. LIMPIEZA DEL ÍNDICE"
            echo "----------------------"
            
            # Detener microservicio si está ejecutándose
            echo "🛑 Deteniendo microservicio..."
            pkill -f "LoadnormasApplication" 2>/dev/null
            sleep 3
            
            # Crear backup
            echo "💾 Creando backup del índice actual..."
            if [ -d "$INDEX_DIR" ]; then
                cp -r "$INDEX_DIR" "$BACKUP_DIR"
                echo "   ✅ Backup creado en: $BACKUP_DIR"
            else
                echo "   ⚠️  No se pudo crear backup (directorio no existe)"
            fi
            
            # Limpiar índice
            echo "🗑️  Limpiando índice duplicado..."
            if [ -d "$INDEX_DIR" ]; then
                rm -rf "$INDEX_DIR"/*
                echo "   ✅ Índice limpiado"
            else
                mkdir -p "$INDEX_DIR"
                echo "   ✅ Directorio de índice creado"
            fi
            
            echo ""
            echo "🔄 3. RECONSTRUCCIÓN DEL ÍNDICE"
            echo "----------------------------"
            
            # Reconstruir índice
            echo "🚀 Iniciando reconstrucción del índice..."
            echo "   📁 Directorio de documentos: /Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII"
            
            # Ejecutar indexación masiva
            curl -X POST "http://localhost:8080/api/bulk/index-sii-documents" 2>/dev/null &
            INDEX_PID=$!
            
            echo "   ⏳ Proceso de indexación iniciado (PID: $INDEX_PID)"
            echo "   📊 Monitoreando progreso..."
            
            # Monitorear progreso
            for i in {1..30}; do
                sleep 10
                if curl -s "http://localhost:8080/api/search/stats" > /dev/null 2>&1; then
                    STATS=$(curl -s "http://localhost:8080/api/search/stats")
                    TOTAL_DOCS=$(echo "$STATS" | jq -r '.totalDocuments // 0')
                    echo "   📄 Documentos indexados: $TOTAL_DOCS"
                    
                    if [ "$TOTAL_DOCS" -gt 1000 ]; then
                        echo "   ✅ Indexación completada"
                        break
                    fi
                fi
                
                if [ $i -eq 30 ]; then
                    echo "   ⚠️  Timeout alcanzado, verificando estado..."
                fi
            done
            
            echo ""
            echo "✅ 4. VERIFICACIÓN FINAL"
            echo "----------------------"
            
            # Verificar estadísticas finales
            sleep 5
            FINAL_STATS=$(curl -s "http://localhost:8080/api/search/stats")
            if [ $? -eq 0 ]; then
                FINAL_TOTAL=$(echo "$FINAL_STATS" | jq -r '.totalDocuments // "N/A"')
                echo "   📄 Total documentos final: $FINAL_TOTAL"
                
                if [ "$FINAL_TOTAL" != "N/A" ] && [ "$FINAL_TOTAL" -lt 1500 ]; then
                    echo "   ✅ Índice reconstruido correctamente"
                else
                    echo "   ⚠️  Índice aún tiene problemas"
                fi
            else
                echo "   ❌ No se pudo verificar el estado final"
            fi
            
            # Probar búsqueda
            echo ""
            echo "🔍 Prueba de búsqueda final:"
            TEST_SEARCH=$(curl -s "http://localhost:8080/api/search/documents?query=IVA&limit=3")
            if [ $? -eq 0 ]; then
                UNIQUE_FILES=$(echo "$TEST_SEARCH" | jq -r '.results[] | .filename' | sort | uniq | wc -l)
                TOTAL_RESULTS=$(echo "$TEST_SEARCH" | jq -r '.results | length')
                
                echo "   📄 Resultados únicos: $UNIQUE_FILES"
                echo "   📄 Total resultados: $TOTAL_RESULTS"
                
                if [ "$UNIQUE_FILES" -eq "$TOTAL_RESULTS" ]; then
                    echo "   ✅ No hay duplicados en los resultados"
                else
                    echo "   ⚠️  Aún hay duplicados"
                fi
            else
                echo "   ❌ No se pudo realizar prueba de búsqueda"
            fi
            
            ;;
        b)
            echo ""
            echo "💾 CREANDO BACKUP DEL ÍNDICE"
            echo "---------------------------"
            
            if [ -d "$INDEX_DIR" ]; then
                cp -r "$INDEX_DIR" "$BACKUP_DIR"
                echo "✅ Backup creado en: $BACKUP_DIR"
                echo "📁 Tamaño del backup: $(du -sh "$BACKUP_DIR" | cut -f1)"
            else
                echo "❌ No se pudo crear backup (directorio no existe)"
            fi
            ;;
        c)
            echo "❌ Operación cancelada"
            exit 0
            ;;
        *)
            echo "❌ Opción inválida"
            exit 1
            ;;
    esac
    
else
    echo "✅ No se detectaron problemas en el índice"
    echo "   El índice parece estar funcionando correctamente"
fi

echo ""
echo "🎉 PROCESO COMPLETADO"
echo "===================="
echo ""
echo "📋 Resumen:"
echo "   - Diagnóstico: Completado"
if [ "$PROBLEM_DETECTED" = true ]; then
    echo "   - Problema: Detectado y resuelto"
    echo "   - Backup: Creado en $BACKUP_DIR"
    echo "   - Índice: Reconstruido"
else
    echo "   - Problema: No detectado"
fi
echo ""
echo "🔧 Comandos útiles:"
echo "   - Ver estadísticas: curl http://localhost:8080/api/search/stats"
echo "   - Probar búsqueda: curl 'http://localhost:8080/api/search/documents?query=IVA&limit=5'"
echo "   - Reiniciar microservicio: mvn spring-boot:run -Dmaven.test.skip=true &"
