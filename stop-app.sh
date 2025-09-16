#!/bin/bash

# Script para detener el Microservicio de Búsqueda de Documentos Normativos SII
# Autor: Sistema de Gestión de Documentos Normativos
# Fecha: $(date +%Y-%m-%d)

echo "🛑 Deteniendo Microservicio de Búsqueda de Documentos Normativos SII..."
echo "📅 Fecha: $(date)"
echo ""

# Función para mostrar ayuda
show_help() {
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help     Mostrar esta ayuda"
    echo "  -f, --force    Forzar terminación (kill -9)"
    echo "  -a, --all      Detener todas las instancias de Spring Boot"
    echo ""
    echo "Ejemplos:"
    echo "  $0              # Detener aplicación de forma suave"
    echo "  $0 --force      # Forzar terminación"
    echo "  $0 --all        # Detener todas las instancias"
}

# Variables por defecto
FORCE=false
ALL_INSTANCES=false

# Procesar argumentos
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -f|--force)
            FORCE=true
            shift
            ;;
        -a|--all)
            ALL_INSTANCES=true
            shift
            ;;
        *)
            echo "❌ Opción desconocida: $1"
            show_help
            exit 1
            ;;
    esac
done

# Buscar procesos de Spring Boot
if [ "$ALL_INSTANCES" = true ]; then
    echo "🔍 Buscando todas las instancias de Spring Boot..."
    PIDS=$(pgrep -f "spring-boot:run")
else
    echo "🔍 Buscando instancia del microservicio normativo..."
    PIDS=$(pgrep -f "loadnormas")
fi

if [ -z "$PIDS" ]; then
    echo "ℹ️  No se encontraron instancias de la aplicación ejecutándose"
    echo "   La aplicación ya está detenida o no se está ejecutando"
    exit 0
fi

echo "📋 Procesos encontrados:"
for PID in $PIDS; do
    echo "   PID: $PID - $(ps -p $PID -o command= | head -c 80)..."
done

echo ""

# Detener procesos
for PID in $PIDS; do
    if [ "$FORCE" = true ]; then
        echo "⚡ Forzando terminación del proceso $PID..."
        if kill -9 $PID 2>/dev/null; then
            echo "✅ Proceso $PID terminado forzosamente"
        else
            echo "❌ No se pudo terminar el proceso $PID"
        fi
    else
        echo "🔄 Enviando señal de terminación suave al proceso $PID..."
        if kill -TERM $PID 2>/dev/null; then
            echo "✅ Señal de terminación enviada al proceso $PID"
            
            # Esperar a que el proceso termine
            echo "⏳ Esperando a que el proceso termine..."
            for i in {1..10}; do
                if ! kill -0 $PID 2>/dev/null; then
                    echo "✅ Proceso $PID terminado exitosamente"
                    break
                fi
                sleep 1
                echo "   Esperando... ($i/10)"
            done
            
            # Si aún está ejecutándose, forzar terminación
            if kill -0 $PID 2>/dev/null; then
                echo "⚠️  El proceso no terminó suavemente, forzando terminación..."
                kill -9 $PID 2>/dev/null
                echo "✅ Proceso $PID terminado forzosamente"
            fi
        else
            echo "❌ No se pudo enviar señal al proceso $PID"
        fi
    fi
done

echo ""

# Verificar que no queden procesos
REMAINING_PIDS=$(pgrep -f "loadnormas")
if [ -z "$REMAINING_PIDS" ]; then
    echo "✅ Microservicio detenido exitosamente"
    echo "🌐 La aplicación ya no está disponible en http://localhost:8080"
else
    echo "⚠️  Algunos procesos podrían seguir ejecutándose:"
    for PID in $REMAINING_PIDS; do
        echo "   PID: $PID"
    done
    echo "   Considera usar la opción --force si es necesario"
fi

echo ""
echo "📊 Resumen:"
echo "   - Aplicación: Microservicio de Búsqueda de Documentos Normativos SII"
echo "   - Puerto: 8080"
echo "   - Estado: Detenido"
echo "   - Para reiniciar: ./start-app.sh"
