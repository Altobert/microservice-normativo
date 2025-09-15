#!/bin/bash

echo "🔍 PRUEBA DE SWAGGER API - MICROSERVICIO DE DOCUMENTOS NORMATIVOS"
echo "================================================================="

BASE_URL="http://localhost:8080"

echo ""
echo "📊 1. VERIFICANDO INFORMACIÓN DE LA API"
echo "--------------------------------------"
echo "Obteniendo información de la API..."
API_INFO=$(curl -s "$BASE_URL/api-docs" | jq -r '.info.title, .info.version, .info.description' | head -3)
echo "✅ Título: $(echo "$API_INFO" | sed -n '1p')"
echo "✅ Versión: $(echo "$API_INFO" | sed -n '2p')"
echo "✅ Descripción: $(echo "$API_INFO" | sed -n '3p' | cut -c1-100)..."

echo ""
echo "🌐 2. VERIFICANDO ENDPOINTS DOCUMENTADOS"
echo "---------------------------------------"
ENDPOINTS=$(curl -s "$BASE_URL/api-docs" | jq -r '.paths | keys[]' | sort)
echo "Endpoints disponibles:"
echo "$ENDPOINTS" | while read endpoint; do
    echo "  📍 $endpoint"
done

echo ""
echo "📋 3. VERIFICANDO TAGS (CATEGORÍAS)"
echo "----------------------------------"
TAGS=$(curl -s "$BASE_URL/api-docs" | jq -r '.tags[].name' 2>/dev/null || echo "No tags found")
echo "Tags disponibles:"
echo "$TAGS" | while read tag; do
    echo "  🏷️  $tag"
done

echo ""
echo "🔧 4. VERIFICANDO OPERACIONES POR ENDPOINT"
echo "------------------------------------------"
echo "$ENDPOINTS" | while read endpoint; do
    echo "📍 $endpoint:"
    METHODS=$(curl -s "$BASE_URL/api-docs" | jq -r ".paths.\"$endpoint\" | keys[]" 2>/dev/null)
    echo "$METHODS" | while read method; do
        SUMMARY=$(curl -s "$BASE_URL/api-docs" | jq -r ".paths.\"$endpoint\".\"$method\".summary // \"Sin resumen\"" 2>/dev/null)
        echo "  🔸 $method: $SUMMARY"
    done
    echo ""
done

echo ""
echo "🌐 5. VERIFICANDO SWAGGER UI"
echo "----------------------------"
if curl -s "$BASE_URL/swagger-ui/index.html" | grep -q "Swagger UI"; then
    echo "✅ Swagger UI disponible en: $BASE_URL/swagger-ui/index.html"
else
    echo "❌ Swagger UI no disponible"
fi

echo ""
echo "📖 6. VERIFICANDO DOCUMENTACIÓN JSON"
echo "------------------------------------"
if curl -s "$BASE_URL/api-docs" | jq -e '.info' > /dev/null 2>&1; then
    echo "✅ Documentación JSON válida"
    DOC_COUNT=$(curl -s "$BASE_URL/api-docs" | jq '.paths | length')
    echo "✅ $DOC_COUNT endpoints documentados"
else
    echo "❌ Documentación JSON inválida"
fi

echo ""
echo "🎯 7. PRUEBA DE ENDPOINTS PRINCIPALES"
echo "-------------------------------------"

# Probar endpoint de estadísticas
echo "🔍 Probando endpoint de estadísticas..."
STATS_RESPONSE=$(curl -s "$BASE_URL/api/search/stats")
if echo "$STATS_RESPONSE" | jq -e '.totalDocuments' > /dev/null 2>&1; then
    TOTAL_DOCS=$(echo "$STATS_RESPONSE" | jq -r '.totalDocuments')
    echo "✅ Estadísticas: $TOTAL_DOCS documentos indexados"
else
    echo "❌ Error en endpoint de estadísticas"
fi

# Probar endpoint de búsqueda
echo "🔍 Probando endpoint de búsqueda..."
SEARCH_RESPONSE=$(curl -s "$BASE_URL/api/search/documents?query=IVA&limit=1")
if echo "$SEARCH_RESPONSE" | jq -e '.results' > /dev/null 2>&1; then
    SEARCH_COUNT=$(echo "$SEARCH_RESPONSE" | jq -r '.totalResults')
    echo "✅ Búsqueda: $SEARCH_COUNT resultados para 'IVA'"
else
    echo "❌ Error en endpoint de búsqueda"
fi

echo ""
echo "📋 8. RESUMEN DE CONFIGURACIÓN"
echo "------------------------------"
echo "✅ API Base URL: $BASE_URL"
echo "✅ Swagger UI: $BASE_URL/swagger-ui/index.html"
echo "✅ API Docs: $BASE_URL/api-docs"
echo "✅ OpenAPI Spec: $BASE_URL/v3/api-docs"

echo ""
echo "🎉 CONFIGURACIÓN DE SWAGGER COMPLETADA EXITOSAMENTE"
echo "=================================================="
echo ""
echo "📖 Para acceder a la documentación interactiva:"
echo "   🌐 Abre tu navegador en: $BASE_URL/swagger-ui/index.html"
echo ""
echo "📋 Para obtener la especificación OpenAPI:"
echo "   📄 JSON: $BASE_URL/api-docs"
echo "   📄 YAML: $BASE_URL/v3/api-docs.yaml"
echo ""
echo "🔧 Para probar los endpoints:"
echo "   🔍 Búsqueda: $BASE_URL/api/search/documents?query=IVA&limit=5"
echo "   📊 Estadísticas: $BASE_URL/api/search/stats"
echo "   📁 Indexación: $BASE_URL/api/bulk/index-sii-documents"
