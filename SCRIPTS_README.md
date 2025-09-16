# Scripts de Gestión del Microservicio

Este directorio contiene scripts de shell para facilitar el manejo del Microservicio de Búsqueda de Documentos Normativos SII.

## 📁 Scripts Disponibles

### 🚀 `start-app.sh` - Iniciar Aplicación

Script para levantar el microservicio con todas las verificaciones necesarias.

**Uso:**
```bash
./start-app.sh
```

**Características:**
- ✅ Verifica que Java y Maven estén instalados
- ✅ Valida que estés en el directorio correcto del proyecto
- ✅ Verifica la existencia del índice de Lucene
- ✅ Compila el proyecto automáticamente
- ✅ Muestra información útil sobre URLs y endpoints
- ✅ Inicia la aplicación en el puerto 8080

**URLs importantes:**
- 📖 Documentación Swagger: http://localhost:8080/swagger-ui.html
- 🔍 API de búsqueda: http://localhost:8080/api/search/documents
- 📊 Estadísticas: http://localhost:8080/api/search/stats

### 🛑 `stop-app.sh` - Detener Aplicación

Script para detener el microservicio de forma segura.

**Uso básico:**
```bash
./stop-app.sh
```

**Opciones avanzadas:**
```bash
./stop-app.sh --help      # Mostrar ayuda
./stop-app.sh --force     # Forzar terminación (kill -9)
./stop-app.sh --all       # Detener todas las instancias de Spring Boot
```

**Características:**
- ✅ Terminación suave por defecto (SIGTERM)
- ✅ Opción de terminación forzada si es necesario
- ✅ Detección automática de procesos
- ✅ Verificación de que la aplicación se detuvo correctamente
- ✅ Soporte para múltiples instancias

## 🔧 Requisitos del Sistema

### Software Necesario
- **Java 17+**: Requerido para ejecutar Spring Boot
- **Maven 3.6+**: Requerido para compilar y ejecutar la aplicación
- **Bash**: Los scripts están optimizados para bash/zsh

### Verificación de Requisitos
Los scripts verifican automáticamente:
- ✅ Presencia de Java en el PATH
- ✅ Presencia de Maven en el PATH
- ✅ Existencia del archivo `pom.xml`
- ✅ Existencia del directorio del índice de Lucene

## 📋 Flujo de Trabajo Recomendado

### 1. Iniciar la Aplicación
```bash
# Desde el directorio del proyecto
./start-app.sh
```

### 2. Usar la Aplicación
- Acceder a la documentación: http://localhost:8080/swagger-ui.html
- Realizar búsquedas: http://localhost:8080/api/search/documents?query=IVA
- Ver estadísticas: http://localhost:8080/api/search/stats

### 3. Detener la Aplicación
```bash
# Terminación suave (recomendado)
./stop-app.sh

# O desde otra terminal
./stop-app.sh --force
```

## 🚨 Solución de Problemas

### Error: "Java no está instalado"
```bash
# Instalar Java (macOS con Homebrew)
brew install openjdk@17

# Agregar al PATH
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
```

### Error: "Maven no está instalado"
```bash
# Instalar Maven (macOS con Homebrew)
brew install maven
```

### Error: "No se encontró el archivo pom.xml"
- Asegúrate de ejecutar los scripts desde el directorio raíz del proyecto
- Verifica que el archivo `pom.xml` existe

### Error: "El directorio del índice no existe"
- Verifica que el path configurado en `application.properties` sea correcto
- Asegúrate de que el índice de Lucene esté disponible

### La aplicación no se detiene
```bash
# Usar terminación forzada
./stop-app.sh --force

# O detener todas las instancias
./stop-app.sh --all
```

## 📊 Información del Proyecto

- **Nombre**: Microservicio de Búsqueda de Documentos Normativos SII
- **Puerto**: 8080
- **Índice**: `/Users/albertosanmartin/usach-memoria-implementacion/desarrollo/proyecto-normativo-ms/pipelinenormativosii/lucene-index`
- **Documentos indexados**: 104 documentos
- **Tecnologías**: Spring Boot 3.4.5, Apache Lucene 9.6.0

## 🔄 Automatización

Para automatizar el inicio/parada del servicio, puedes:

### Crear alias en tu shell
```bash
# Agregar a ~/.zshrc o ~/.bashrc
alias start-normativo="cd /Users/albertosanmartin/usach-memoria-implementacion/desarrollo/proyecto-normativo-ms/microservice-normativo && ./start-app.sh"
alias stop-normativo="cd /Users/albertosanmartin/usach-memoria-implementacion/desarrollo/proyecto-normativo-ms/microservice-normativo && ./stop-app.sh"
```

### Usar con cron (inicio automático)
```bash
# Editar crontab
crontab -e

# Agregar línea para inicio automático (ejemplo: todos los días a las 9:00 AM)
0 9 * * * cd /Users/albertosanmartin/usach-memoria-implementacion/desarrollo/proyecto-normativo-ms/microservice-normativo && ./start-app.sh
```

---

**Nota**: Estos scripts están diseñados para facilitar el desarrollo y testing. Para producción, considera usar herramientas como systemd, Docker, o servicios de gestión de aplicaciones.
