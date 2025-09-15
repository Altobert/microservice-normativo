#!/bin/bash

echo "📊 MONITOR DE TRABAJOS ZSH - $(date)"
echo "=================================="

# Función para mostrar información de trabajos
show_jobs_info() {
    echo ""
    echo "🔍 Trabajos activos:"
    local job_count=$(jobs | wc -l)
    if [ $job_count -gt 0 ]; then
        jobs -l
    else
        echo "   ✅ No hay trabajos activos"
    fi
    
    echo ""
    echo "🔍 Procesos Java:"
    local java_count=$(ps aux | grep java | grep -v grep | wc -l)
    if [ $java_count -gt 0 ]; then
        ps aux | grep java | grep -v grep | head -5
    else
        echo "   ✅ No hay procesos Java ejecutándose"
    fi
    
    echo ""
    echo "🔍 Procesos Maven:"
    local maven_count=$(ps aux | grep maven | grep -v grep | wc -l)
    if [ $maven_count -gt 0 ]; then
        ps aux | grep maven | grep -v grep | head -5
    else
        echo "   ✅ No hay procesos Maven ejecutándose"
    fi
    
    echo ""
    echo "🔍 Procesos del proyecto:"
    local project_count=$(ps aux | grep -E "(LoadnormasApplication|microservice-normativo)" | grep -v grep | wc -l)
    if [ $project_count -gt 0 ]; then
        ps aux | grep -E "(LoadnormasApplication|microservice-normativo)" | grep -v grep
    else
        echo "   ✅ No hay procesos del proyecto ejecutándose"
    fi
}

# Función para mostrar estadísticas
show_stats() {
    echo ""
    echo "📈 Estadísticas:"
    echo "   Trabajos activos: $(jobs | wc -l)"
    echo "   Procesos Java: $(ps aux | grep java | grep -v grep | wc -l)"
    echo "   Procesos Maven: $(ps aux | grep maven | grep -v grep | wc -l)"
    echo "   Procesos del proyecto: $(ps aux | grep -E "(LoadnormasApplication|microservice-normativo)" | grep -v grep | wc -l)"
}

# Función para mostrar recomendaciones
show_recommendations() {
    local job_count=$(jobs | wc -l)
    if [ $job_count -gt 0 ]; then
        echo ""
        echo "⚠️  Recomendaciones:"
        echo "   - Hay trabajos activos que podrían causar 'zsh: you have running jobs'"
        echo "   - Usa './fix_zsh_jobs.sh' para resolver el problema"
        echo "   - O termina trabajos manualmente con 'kill %<número>'"
    else
        echo ""
        echo "✅ Estado saludable:"
        echo "   - No hay trabajos activos"
        echo "   - Puedes salir normalmente con 'exit'"
    fi
}

# Función principal
main() {
    show_jobs_info
    show_stats
    show_recommendations
    
    echo ""
    echo "⏰ Actualizando en 5 segundos... (Ctrl+C para salir)"
    sleep 5
}

# Función de ayuda
show_help() {
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help     Mostrar esta ayuda"
    echo "  -o, --once     Mostrar información una sola vez"
    echo "  -s, --stats    Solo mostrar estadísticas"
    echo "  -c, --continuous  Monitoreo continuo (por defecto)"
    echo ""
    echo "Ejemplos:"
    echo "  $0              # Monitoreo continuo"
    echo "  $0 --once       # Mostrar información una vez"
    echo "  $0 --stats      # Solo estadísticas"
}

# Procesar argumentos
case "$1" in
    -h|--help)
        show_help
        exit 0
        ;;
    -o|--once)
        show_jobs_info
        show_stats
        show_recommendations
        exit 0
        ;;
    -s|--stats)
        show_stats
        exit 0
        ;;
    -c|--continuous|"")
        # Monitoreo continuo
        while true; do
            clear
            main
        done
        ;;
    *)
        echo "❌ Opción desconocida: $1"
        show_help
        exit 1
        ;;
esac
