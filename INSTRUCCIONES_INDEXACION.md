# 📚 Instrucciones de Indexación de Documentos

## 🎯 **Problema Resuelto**

**Antes:** Cada vez que ejecutabas tests, se indexaban automáticamente todos los documentos (104 archivos), causando:
- ⏱️ Tiempo de ejecución lento en tests
- 🔄 Indexación innecesaria repetitiva
- 📊 Logs confusos mezclando tests con indexación

**Ahora:** La indexación solo se ejecuta cuando es necesaria, manteniendo los tests rápidos y limpios.

## 🚀 **Cómo Ejecutar la Indexación**

### **Opción 1: Script de Shell (Recomendado)**
```bash
# Indexar con directorio por defecto
./index-documents.sh

# Indexar directorio específico
./index-documents.sh /ruta/a/tu/directorio
```

### **Opción 2: Maven Exec**
```bash
# Indexar con directorio por defecto
mvn exec:java -Dexec.mainClass="cl.sii.normativo.loadnormas.IndexDocumentsRunner" -Dmaven.test.skip=true

# Indexar directorio específico
mvn exec:java -Dexec.mainClass="cl.sii.normativo.loadnormas.IndexDocumentsRunner" -Dexec.args="/ruta/a/tu/directorio" -Dmaven.test.skip=true
```

### **Opción 3: Perfil Spring (Para desarrollo)**
```bash
# Activar perfil 'index' para ejecutar indexación automática
mvn spring-boot:run -Dspring-boot.run.profiles=index
```

## 🧪 **Ejecutar Tests Sin Indexación**

### **Tests Rápidos (Sin Indexación)**
```bash
# Ejecutar todos los tests sin indexación automática
mvn test

# Ejecutar tests específicos
mvn test -Dtest=LuceneIndexerTest
```

### **Tests con Indexación (Solo si es necesario)**
```bash
# Ejecutar tests con perfil de indexación
mvn test -Dspring-boot.run.profiles=index
```

## 📋 **Flujo de Trabajo Recomendado**

### **1. Primera Vez (Indexación Inicial)**
```bash
# 1. Ejecutar indexación una sola vez
./index-documents.sh

# 2. Verificar que funcionó
curl "http://localhost:8080/api/search/stats"
```

### **2. Desarrollo Diario**
```bash
# Ejecutar tests rápidos (sin indexación)
mvn test

# Ejecutar aplicación (sin indexación automática)
mvn spring-boot:run
```

### **3. Cuando Necesites Re-indexar**
```bash
# Solo cuando cambies documentos o necesites limpiar el índice
./index-documents.sh
```

## 🔧 **Configuración Técnica**

### **Archivos Modificados:**

1. **`IndexDocumentsScript.java`**
   - ✅ Agregado `@Profile("index")` 
   - ✅ Solo se ejecuta con perfil activo

2. **`IndexDocumentsRunner.java`** (Nuevo)
   - ✅ Aplicación standalone para indexación
   - ✅ Se ejecuta solo cuando se llama explícitamente

3. **`index-documents.sh`** (Nuevo)
   - ✅ Script de conveniencia para indexación
   - ✅ Verificaciones de directorio

### **Perfiles Spring:**
- **`default`**: Sin indexación automática (para tests y desarrollo)
- **`index`**: Con indexación automática (solo cuando se necesita)

## 📊 **Verificación de Estado**

### **Verificar Documentos Indexados**
```bash
curl "http://localhost:8080/api/search/stats" | jq .
```

### **Buscar Documentos**
```bash
curl "http://localhost:8080/api/search/documents?query=IVA&limit=5" | jq .
```

### **Verificar Logs**
```bash
# Los tests ahora deberían ser más rápidos y sin logs de indexación
mvn test
```

## 🎉 **Beneficios**

- ⚡ **Tests más rápidos**: Sin indexación automática
- 🧹 **Logs más limpios**: Separación clara entre tests e indexación
- 🎯 **Control total**: Indexación solo cuando la necesitas
- 🔄 **Flexibilidad**: Múltiples formas de ejecutar indexación
- 📈 **Eficiencia**: Una sola indexación inicial, tests rápidos después

## 🚨 **Notas Importantes**

1. **Primera ejecución**: Debes ejecutar la indexación al menos una vez
2. **Tests**: Ahora son independientes de la indexación
3. **Desarrollo**: La aplicación funciona sin indexación automática
4. **Re-indexación**: Solo cuando cambies documentos o necesites limpiar

¡Ahora tienes control total sobre cuándo se ejecuta la indexación! 🎯
