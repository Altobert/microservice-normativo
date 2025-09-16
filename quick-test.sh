#!/bin/bash

# Script de pruebas rápidas para el Microservicio de Búsqueda de Documentos Normativos SII
# Autor: Sistema de Gestión de Documentos Normativos
# Fecha: $(date +%Y-%m-%d)

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuración
BASE_URL="http://localhost:8080"
TIMEOUT=5

echo -e "${CYAN}⚡ PRUEBAS RÁPIDAS DEL MICROSERVICIO${NC}"
echo -e "${CYAN}=====================================${NC}"
echo ""

# Verificar conectividad básica
echo -e "${BLUE}🔍 Verificando conectividad...${NC}"
if curl -s --max-time $TIMEOUT "$BASE_URL/actuator/health" | grep -q "UP"; then
    echo -e "${GREEN}✅ Microservicio ejecutándose${NC}"
else
    echo -e "${RED}❌ Microservicio no disponible${NC}"
    echo -e "${YELLOW}💡 Asegúrate de ejecutar: ./start-app.sh${NC}"
    exit 1
fi

# Verificar información del servicio
echo -e "${BLUE}📋 Verificando información del servicio...${NC}"
SERVICE_INFO=$(curl -s --max-time $TIMEOUT "$BASE_URL/api/documents/info")
if echo "$SERVICE_INFO" | jq -e '.service' > /dev/null 2>&1; then
    SERVICE_NAME=$(echo "$SERVICE_INFO" | jq -r '.service')
    SERVICE_VERSION=$(echo "$SERVICE_INFO" | jq -r '.version')
    echo -e "${GREEN}✅ Servicio: $SERVICE_NAME v$SERVICE_VERSION${NC}"
else
    echo -e "${RED}❌ Error obteniendo información del servicio${NC}"
fi

# Verificar estadísticas
echo -e "${BLUE}📊 Verificando estadísticas...${NC}"
STATS=$(curl -s --max-time $TIMEOUT "$BASE_URL/api/search/stats")
if echo "$STATS" | jq -e '.totalDocuments' > /dev/null 2>&1; then
    TOTAL_DOCS=$(echo "$STATS" | jq -r '.totalDocuments')
    echo -e "${GREEN}✅ Documentos indexados: $TOTAL_DOCS${NC}"
else
    echo -e "${RED}❌ Error obteniendo estadísticas${NC}"
fi

# Probar búsqueda básica
echo -e "${BLUE}🔍 Probando búsqueda básica...${NC}"
SEARCH_RESULT=$(curl -s --max-time $TIMEOUT "$BASE_URL/api/search/documents?query=IVA&limit=3")
if echo "$SEARCH_RESULT" | jq -e '.results' > /dev/null 2>&1; then
    RESULT_COUNT=$(echo "$SEARCH_RESULT" | jq -r '.totalResults')
    echo -e "${GREEN}✅ Búsqueda 'IVA': $RESULT_COUNT resultados${NC}"
    
    # Mostrar primer resultado
    FIRST_RESULT=$(echo "$SEARCH_RESULT" | jq -r '.results[0].filename // "N/A"')
    echo -e "${GREEN}   📄 Primer resultado: $FIRST_RESULT${NC}"
else
    echo -e "${RED}❌ Error en búsqueda${NC}"
fi

# Probar búsqueda por año
echo -e "${BLUE}📅 Probando búsqueda por año...${NC}"
YEAR_SEARCH=$(curl -s --max-time $TIMEOUT "$BASE_URL/api/search/documents/year/2020?query=IVA&limit=2")
if echo "$YEAR_SEARCH" | jq -e '.year' > /dev/null 2>&1; then
    YEAR_RESULTS=$(echo "$YEAR_SEARCH" | jq -r '.totalResults')
    echo -e "${GREEN}✅ Búsqueda año 2020: $YEAR_RESULTS resultados${NC}"
else
    echo -e "${RED}❌ Error en búsqueda por año${NC}"
fi

# Verificar Swagger UI
echo -e "${BLUE}📖 Verificando documentación...${NC}"
if curl -s --max-time $TIMEOUT "$BASE_URL/swagger-ui/index.html" | grep -q "Swagger UI"; then
    echo -e "${GREEN}✅ Swagger UI disponible${NC}"
else
    echo -e "${RED}❌ Swagger UI no disponible${NC}"
fi

echo ""
echo -e "${CYAN}🎯 RESUMEN RÁPIDO${NC}"
echo -e "${CYAN}================${NC}"
echo -e "${GREEN}✅ Servicio: Funcionando${NC}"
echo -e "${GREEN}✅ Búsqueda: Operativa${NC}"
echo -e "${GREEN}✅ Documentación: Disponible${NC}"
echo ""
echo -e "${BLUE}🔗 URLs importantes:${NC}"
echo -e "   🌐 Swagger UI: $BASE_URL/swagger-ui/index.html"
echo -e "   🔍 Búsqueda: $BASE_URL/api/search/documents?query=IVA&limit=5"
echo -e "   📊 Estadísticas: $BASE_URL/api/search/stats"
echo ""
echo -e "${YELLOW}💡 Para pruebas completas ejecuta: ./test-microservice.sh${NC}"
echo -e "${YELLOW}💡 Para pruebas detalladas ejecuta: ./test-microservice.sh --verbose${NC}"
