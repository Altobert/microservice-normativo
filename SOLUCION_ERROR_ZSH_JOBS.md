# 🔧 Solución al Error: "zsh: you have running jobs"

## 📋 Descripción del Problema

### **Error Encontrado:**
```bash
➜  microservice-normativo git:(develop-normativo) exit
zsh: you have running jobs.
```

### **Contexto:**
Este error ocurre cuando intentas salir de una sesión de zsh (`exit`) pero hay procesos ejecutándose en segundo plano que zsh considera como "trabajos activos". El shell te advierte que hay procesos que podrían quedar huérfanos si sales de la sesión.

## 🔍 Causas del Problema

### **1. Procesos en Segundo Plano**
- Procesos iniciados con `&` al final del comando
- Servicios ejecutándose en background
- Aplicaciones Java/Maven ejecutándose
- Servidores web o APIs activas

### **2. Procesos Específicos en Nuestro Caso**
```bash
# Microservicio Spring Boot ejecutándose
PID 65024: LoadnormasApplication (Java)

# Maven ejecutando spring-boot:run
PID 62274: Maven spring-boot:run
```

### **3. Detección por zsh**
zsh detecta estos procesos como "jobs" activos y previene la salida para evitar:
- Procesos huérfanos
- Pérdida de datos
- Conexiones abiertas sin cerrar

## 🛠️ Soluciones Implementadas

### **Solución 1: Terminar Procesos Específicos**

#### **Terminar el Microservicio:**
```bash
pkill -f "LoadnormasApplication"
```

#### **Terminar Maven:**
```bash
pkill -f "spring-boot:run"
```

#### **Verificar que no hay trabajos activos:**
```bash
jobs -l
```

### **Solución 2: Comandos de Gestión de Jobs**

#### **Ver trabajos activos:**
```bash
jobs          # Lista trabajos básicos
jobs -l       # Lista trabajos con PIDs
jobs -p       # Solo muestra PIDs
```

#### **Terminar trabajos específicos:**
```bash
kill %1       # Termina trabajo número 1
kill %2       # Termina trabajo número 2
kill %-       # Termina el último trabajo iniciado
```

#### **Traer trabajos al foreground:**
```bash
fg %1         # Trae trabajo 1 al foreground
fg            # Trae el último trabajo al foreground
```

### **Solución 3: Forzar Salida (No Recomendado)**
```bash
exit --force  # Fuerza la salida (puede dejar procesos huérfanos)
```

## 📊 Diagnóstico del Problema

### **Comando de Diagnóstico:**
```bash
# Verificar procesos Java ejecutándose
ps aux | grep java | grep -v grep

# Verificar trabajos de zsh
jobs -l

# Verificar procesos específicos del proyecto
ps aux | grep -E "(LoadnormasApplication|spring-boot:run)" | grep -v grep
```

### **Salida Esperada:**
```bash
# Sin procesos activos (correcto)
# No hay salida

# Con procesos activos (problema)
albertosanmartin 65024   0.0  1.4 416899888 242032 s052  SN    1:42AM   0:51.43 ... LoadnormasApplication
albertosanmartin 62274   0.0  0.1 413987744  20160 s052  SN    1:42AM   0:04.93 ... spring-boot:run
```

## 🚀 Prevención del Problema

### **1. Ejecutar Procesos Correctamente**

#### **Opción A: Con nohup (Recomendado)**
```bash
# Ejecutar en segundo plano que sobrevive al logout
nohup mvn spring-boot:run -Dmaven.test.skip=true > microservice.log 2>&1 &

# Verificar que se ejecuta
ps aux | grep LoadnormasApplication
```

#### **Opción B: Con screen/tmux**
```bash
# Crear sesión screen
screen -S microservice

# Ejecutar comando
mvn spring-boot:run -Dmaven.test.skip=true

# Desacoplar sesión (Ctrl+A, luego D)
# Reconectar: screen -r microservice
```

#### **Opción C: Con systemd (Producción)**
```bash
# Crear servicio systemd
sudo systemctl create microservice-normativo.service

# Habilitar y iniciar
sudo systemctl enable microservice-normativo.service
sudo systemctl start microservice-normativo.service
```

### **2. Gestión Adecuada de Procesos**

#### **Verificar antes de salir:**
```bash
# Script de verificación
check_jobs() {
    if jobs | grep -q "Running"; then
        echo "⚠️  Hay trabajos activos:"
        jobs -l
        echo "¿Deseas terminarlos? (y/n)"
        read response
        if [[ $response == "y" ]]; then
            jobs | awk '{print $1}' | sed 's/[^0-9]//g' | xargs -I {} kill %{}
        fi
    else
        echo "✅ No hay trabajos activos"
    fi
}
```

#### **Alias útil para zsh:**
```bash
# Agregar a ~/.zshrc
alias safe-exit='check_jobs && exit'
alias kill-jobs='jobs | awk "{print \$1}" | sed "s/[^0-9]//g" | xargs -I {} kill %{}'
```

## 📋 Script de Solución Automática

### **Script: `fix_zsh_jobs.sh`**
```bash
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
                check_jobs
                ;;
            b)
                kill_all_jobs
                sleep 2
                check_jobs
                ;;
            c)
                echo "📊 Información de procesos:"
                ps aux | grep -E "(java|maven)" | grep -v grep
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
        echo "⚠️  Aún hay trabajos activos. Puedes usar 'exit --force' para salir forzadamente."
    else
        echo "🎉 ¡Problema resuelto! Puedes salir normalmente con 'exit'"
    fi
}

# Ejecutar función principal
main
```

## 🎯 Casos de Uso Comunes

### **Caso 1: Desarrollo Local**
```bash
# Problema: Microservicio ejecutándose en desarrollo
mvn spring-boot:run &
# ... trabajo ...
exit  # Error: you have running jobs

# Solución:
pkill -f "spring-boot:run"
exit
```

### **Caso 2: Servicios en Producción**
```bash
# Problema: Servicios ejecutándose en producción
nohup java -jar app.jar &
# ... trabajo ...
exit  # Error: you have running jobs

# Solución:
pkill -f "app.jar"
# O mejor: usar systemd/supervisor
```

### **Caso 3: Múltiples Trabajos**
```bash
# Problema: Varios trabajos activos
job1 &
job2 &
job3 &
exit  # Error: you have running jobs

# Solución:
jobs -l
kill %1 %2 %3
exit
```

## 📈 Métricas y Monitoreo

### **Script de Monitoreo:**
```bash
#!/bin/bash
# monitor_jobs.sh

while true; do
    clear
    echo "📊 MONITOR DE TRABAJOS ZSH - $(date)"
    echo "=================================="
    
    echo ""
    echo "🔍 Trabajos activos:"
    jobs -l
    
    echo ""
    echo "🔍 Procesos Java:"
    ps aux | grep java | grep -v grep | head -5
    
    echo ""
    echo "🔍 Procesos Maven:"
    ps aux | grep maven | grep -v grep | head -5
    
    echo ""
    echo "⏰ Actualizando en 5 segundos... (Ctrl+C para salir)"
    sleep 5
done
```

## 🚨 Troubleshooting Avanzado

### **Problema: Procesos que no se terminan**
```bash
# Verificar procesos zombie
ps aux | grep -E "<defunct>|<zombie>"

# Terminar procesos específicos por PID
ps aux | grep LoadnormasApplication
kill -9 <PID>

# Verificar procesos hijos
pstree -p <PID_PADRE>
```

### **Problema: Permisos insuficientes**
```bash
# Verificar permisos
ls -la /proc/<PID>/

# Usar sudo si es necesario
sudo pkill -f "LoadnormasApplication"
```

### **Problema: Procesos en diferentes usuarios**
```bash
# Verificar todos los usuarios
ps aux | grep LoadnormasApplication

# Terminar procesos de usuario específico
sudo -u <usuario> pkill -f "LoadnormasApplication"
```

## 📚 Referencias y Recursos

### **Documentación Oficial:**
- [zsh Jobs Documentation](https://zsh.sourceforge.io/Doc/Release/Jobs-_0026-Signals.html)
- [Bash Job Control](https://www.gnu.org/software/bash/manual/html_node/Job-Control.html)

### **Herramientas Útiles:**
- `htop` - Monitor de procesos interactivo
- `screen` - Gestor de sesiones terminal
- `tmux` - Terminal multiplexer
- `systemd` - Gestor de servicios en Linux

### **Comandos de Referencia:**
```bash
# Gestión de trabajos
jobs, fg, bg, kill, wait

# Gestión de procesos
ps, pkill, pgrep, killall

# Gestión de sesiones
screen, tmux, nohup, disown
```

---

## ✅ Resumen de la Solución

**Problema:** `zsh: you have running jobs` al intentar salir de la terminal

**Causa:** Procesos en segundo plano (microservicio Spring Boot y Maven) ejecutándose

**Solución Aplicada:**
1. `pkill -f "LoadnormasApplication"` - Terminar microservicio
2. `pkill -f "spring-boot:run"` - Terminar Maven
3. `jobs -l` - Verificar que no hay trabajos activos
4. `exit` - Salir normalmente

**Prevención:** Usar `nohup`, `screen`, o `systemd` para procesos de larga duración

**Estado:** ✅ **Problema resuelto exitosamente**

---

*Documentación generada el $(date) - Microservicio de Documentos Normativos SII*
