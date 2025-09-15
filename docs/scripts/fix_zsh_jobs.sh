#!/bin/bash

echo "🔧 SOLUCIONADOR DE ERROR 'zsh: you have running jobs'"
echo "=================================================="

# Función para verificar trabajos activos
check_jobs() {
    local job_count=$(jobs | wc -l)
    if [ $job_count -gt 0 ]; then
        echo "📋 Trabajos activos encontrados:"
        jobs -l
        return 0
    else
        echo "✅ No hay trabajos activos"
        return 1
    fi
}

# Función para terminar trabajos específicos
kill_specific_jobs() {
    echo "🎯 Terminando procesos específicos..."
    
    # Terminar microservicio
    if pgrep -f "LoadnormasApplication" > /dev/null; then
        echo "🛑 Terminando LoadnormasApplication..."
        pkill -f "LoadnormasApplication"
    fi
    
    # Terminar Maven
    if pgrep -f "spring-boot:run" > /dev/null; then
        echo "🛑 Terminando Maven spring-boot:run..."
        pkill -f "spring-boot:run"
    fi
    
    # Terminar otros procesos Java del proyecto
    if pgrep -f "microservice-normativo" > /dev/null; then
        echo "🛑 Terminando procesos del microservicio..."
        pkill -f "microservice-normativo"
    fi
}

# Función para terminar todos los trabajos
kill_all_jobs() {
    echo "🛑 Terminando todos los trabajos activos..."
    jobs | awk '{print $1}' | sed 's/[^0-9]//g' | while read job_num; do
        if [ ! -z "$job_num" ]; then
            echo "Terminando trabajo %$job_num"
            kill %$job_num 2>/dev/null
        fi
    done
}

# Función para mostrar información detallada
show_info() {
    echo "📊 Información detallada de procesos:"
    echo ""
    echo "🔍 Trabajos de zsh:"
    jobs -l
    echo ""
    echo "🔍 Procesos Java:"
    ps aux | grep java | grep -v grep | head -10
    echo ""
    echo "🔍 Procesos Maven:"
    ps aux | grep maven | grep -v grep | head -5
    echo ""
    echo "🔍 Procesos del proyecto:"
    ps aux | grep -E "(LoadnormasApplication|microservice-normativo)" | grep -v grep
}

# Función principal
main() {
    echo ""
    echo "1. Verificando trabajos activos..."
    if check_jobs; then
        echo ""
        echo "2. ¿Qué deseas hacer?"
        echo "   a) Terminar procesos específicos del proyecto"
        echo "   b) Terminar todos los trabajos"
        echo "   c) Solo mostrar información"
        echo "   d) Salir sin hacer nada"
        echo ""
        read -p "Selecciona una opción (a/b/c/d): " choice
        
        case $choice in
            a)
                kill_specific_jobs
                sleep 2
                echo ""
                echo "✅ Verificación después de terminar procesos específicos:"
                check_jobs
                ;;
            b)
                kill_all_jobs
                sleep 2
                echo ""
                echo "✅ Verificación después de terminar todos los trabajos:"
                check_jobs
                ;;
            c)
                show_info
                ;;
            d)
                echo "👋 Saliendo sin cambios..."
                exit 0
                ;;
            *)
                echo "❌ Opción inválida"
                exit 1
                ;;
        esac
    fi
    
    echo ""
    echo "✅ Verificación final:"
    if check_jobs; then
        echo "⚠️  Aún hay trabajos activos. Opciones disponibles:"
        echo "   - Usar 'exit --force' para salir forzadamente"
        echo "   - Ejecutar este script nuevamente"
        echo "   - Terminar trabajos manualmente con 'kill %<número>'"
    else
        echo "🎉 ¡Problema resuelto! Puedes salir normalmente con 'exit'"
    fi
}

# Función de ayuda
show_help() {
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help     Mostrar esta ayuda"
    echo "  -a, --auto     Ejecutar automáticamente (terminar procesos específicos)"
    echo "  -i, --info     Solo mostrar información"
    echo "  -k, --kill     Terminar todos los trabajos automáticamente"
    echo ""
    echo "Ejemplos:"
    echo "  $0              # Modo interactivo"
    echo "  $0 --auto       # Terminar procesos específicos automáticamente"
    echo "  $0 --info       # Solo mostrar información"
    echo "  $0 --kill       # Terminar todos los trabajos"
}

# Procesar argumentos de línea de comandos
case "$1" in
    -h|--help)
        show_help
        exit 0
        ;;
    -a|--auto)
        echo "🤖 Modo automático: terminando procesos específicos..."
        kill_specific_jobs
        sleep 2
        check_jobs
        ;;
    -i|--info)
        show_info
        exit 0
        ;;
    -k|--kill)
        echo "🤖 Modo automático: terminando todos los trabajos..."
        kill_all_jobs
        sleep 2
        check_jobs
        ;;
    "")
        main
        ;;
    *)
        echo "❌ Opción desconocida: $1"
        show_help
        exit 1
        ;;
esac
