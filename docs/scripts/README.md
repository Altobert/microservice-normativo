# 📜 Scripts de Automatización

Esta carpeta contiene todos los scripts de automatización y utilidades del proyecto.

## 🛠️ Scripts Disponibles

### 📊 **analyze_duplicates.sh**
- **Propósito**: Analizar documentos duplicados en el índice
- **Uso**: `./analyze_duplicates.sh`
- **Descripción**: Identifica y reporta documentos duplicados en Lucene

### 🧹 **clean_lucene_index.sh**
- **Propósito**: Limpiar el índice de Lucene
- **Uso**: `./clean_lucene_index.sh`
- **Descripción**: Elimina completamente el índice para empezar desde cero

### 🔍 **diagnostico_sistema.sh**
- **Propósito**: Diagnóstico completo del sistema
- **Uso**: `./diagnostico_sistema.sh`
- **Descripción**: Verifica el estado del sistema y componentes

### 🐚 **fix_zsh_jobs.sh**
- **Propósito**: Solucionar problemas de zsh jobs
- **Uso**: `./fix_zsh_jobs.sh`
- **Descripción**: Termina procesos activos que impiden salir de zsh

### 📄 **index-documents.sh**
- **Propósito**: Script principal para indexar documentos
- **Uso**: `./index-documents.sh`
- **Descripción**: Indexa todos los documentos PDF del directorio configurado

### ⚠️ **monitor_errores.sh**
- **Propósito**: Monitorear errores del sistema
- **Uso**: `./monitor_errores.sh`
- **Descripción**: Vigila y reporta errores en tiempo real

### 📈 **monitor_jobs.sh**
- **Propósito**: Monitorear trabajos activos
- **Uso**: `./monitor_jobs.sh`
- **Descripción**: Supervisa procesos y trabajos en ejecución

### 🔌 **test_swagger_api.sh**
- **Propósito**: Probar la API Swagger
- **Uso**: `./test_swagger_api.sh`
- **Descripción**: Ejecuta pruebas básicas de la API

## 🚀 **Scripts Más Utilizados**

### Para indexar documentos:
```bash
./docs/scripts/index-documents.sh
```

### Para limpiar el índice:
```bash
./docs/scripts/clean_lucene_index.sh
```

### Para diagnosticar problemas:
```bash
./docs/scripts/diagnostico_sistema.sh
```

## ⚡ **Permisos de Ejecución**

Todos los scripts tienen permisos de ejecución configurados. Si necesitas otorgarlos manualmente:

```bash
chmod +x docs/scripts/*.sh
```
