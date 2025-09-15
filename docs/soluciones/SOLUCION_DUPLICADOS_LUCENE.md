# 🔧 Solución al Problema de Duplicados en el Índice de Lucene

## 📋 Problema Identificado

### **Síntomas:**
- **2,210 documentos** indexados cuando solo hay **104 archivos PDF**
- **Ratio de 3:1** (312 documentos indexados vs 104 archivos)
- **Resultados duplicados** en búsquedas (mismo documento aparece 3 veces)
- **Error de lock** de Lucene durante la indexación

### **Causa Raíz:**
El problema se debe a múltiples factores:

1. **Falta de deduplicación**: El `LuceneIndexer` no verifica si un documento ya existe
2. **Concurrencia**: Múltiples `IndexWriter` intentan acceder al mismo índice simultáneamente
3. **Indexación múltiple**: El mismo archivo se indexa varias veces sin control

## 🛠️ Soluciones Implementadas

### **1. Modificación del LuceneIndexer**

#### **Antes:**
```java
public void indexFile(String fileName, String content) throws IOException {
    // Siempre agregaba documentos sin verificar duplicados
    writer.addDocument(document);
}
```

#### **Después:**
```java
public void indexFile(String fileName, String content, String filePath) throws IOException {
    // Usa updateDocument para evitar duplicados basado en filename
    Term term = new Term("filename", fileName);
    writer.updateDocument(term, document);
}
```

#### **Mejoras Implementadas:**
- ✅ **Deduplicación automática** usando `updateDocument`
- ✅ **Extracción de metadatos** (año, documentId) del filename
- ✅ **Títulos legibles** generados automáticamente
- ✅ **Campos StringField** para evitar análisis innecesario

### **2. Modificación del BulkIndexerService**

#### **Antes:**
```java
// Creaba su propio IndexWriter
try (IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
    writer.addDocument(document);
}
```

#### **Después:**
```java
// Usa el LuceneIndexer con deduplicación
luceneIndexer.indexFile(pdfFile.getName(), content, pdfFile.getAbsolutePath());
```

### **3. Scripts de Diagnóstico y Limpieza**

#### **Script de Análisis:**
```bash
./analyze_duplicates.sh
```
- Detecta duplicados en archivos fuente
- Analiza el índice de Lucene
- Prueba búsquedas para detectar duplicados
- Proporciona recomendaciones

#### **Script de Limpieza:**
```bash
./clean_lucene_index.sh
```
- Diagnostica problemas automáticamente
- Crea backup del índice actual
- Limpia el índice duplicado
- Reconstruye el índice desde cero

## 📊 Resultados Obtenidos

### **Antes de la Solución:**
```json
{
  "totalDocuments": 2210,
  "documentsByYear": {
    "2019": 84,
    "2018": 105,
    "2017": 147,
    "2016": 672,
    "2024": 126,
    "2021": 462,
    "2020": 588
  }
}
```

### **Después de la Limpieza:**
```json
{
  "totalDocuments": 312,
  "documentsByYear": {
    "2019": 12,
    "2018": 15,
    "2017": 21,
    "2016": 96,
    "2024": 18,
    "2021": 66,
    "2020": 84
  }
}
```

### **Problema Persistente:**
- Aún hay **312 documentos** cuando solo hay **104 archivos**
- Cada documento aparece **3 veces** en las búsquedas
- **Error de lock** de Lucene impide la indexación correcta

## 🔧 Solución Final Requerida

### **Problema del Lock de Lucene:**
El error `Lock held by this virtual machine` indica que múltiples procesos intentan acceder al índice simultáneamente.

### **Solución Propuesta:**
1. **Modificar BulkIndexerService** para usar un solo `IndexWriter`
2. **Implementar sincronización** para evitar concurrencia
3. **Usar IndexWriterConfig** con configuración de lock apropiada

### **Código de Solución:**
```java
@Service
public class BulkIndexerService {
    
    @Autowired
    private LuceneIndexer luceneIndexer;
    
    public BulkIndexResult indexDirectory(String directoryPath) throws IOException {
        // Usar un solo IndexWriter para toda la operación
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             IndexWriter writer = new IndexWriter(dir, createIndexWriterConfig())) {
            
            List<File> pdfFiles = findPDFFiles(new File(directoryPath));
            
            for (File pdfFile : pdfFiles) {
                try {
                    String content = pdfTextExtractor.extractText(new FileInputStream(pdfFile));
                    
                    // Crear documento con deduplicación
                    Document document = createDocument(pdfFile, content);
                    Term term = new Term("filename", pdfFile.getName());
                    writer.updateDocument(term, document);
                    
                } catch (Exception e) {
                    // Manejar errores individuales
                }
            }
            
            // Commit explícito
            writer.commit();
        }
    }
    
    private IndexWriterConfig createIndexWriterConfig() {
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return config;
    }
}
```

## 📋 Pasos para Resolver Completamente

### **1. Implementar Solución de Concurrencia**
- Modificar `BulkIndexerService` para usar un solo `IndexWriter`
- Implementar sincronización adecuada
- Configurar locks de Lucene apropiadamente

### **2. Limpiar Índice Completamente**
```bash
# Detener microservicio
pkill -f "LoadnormasApplication"

# Limpiar índice
rm -rf path/to/index/*

# Reiniciar microservicio
mvn spring-boot:run -Dmaven.test.skip=true &

# Reindexar
curl -X POST "http://localhost:8080/api/bulk/index-sii-documents"
```

### **3. Verificar Resultados**
```bash
# Verificar estadísticas
curl "http://localhost:8080/api/search/stats"

# Probar búsqueda
curl "http://localhost:8080/api/search/documents?query=IVA&limit=5"

# Analizar duplicados
./analyze_duplicates.sh
```

## 🎯 Estado Actual

### **Problemas Resueltos:**
- ✅ **Diagnóstico completo** del problema
- ✅ **Scripts de análisis** y limpieza creados
- ✅ **Modificaciones al LuceneIndexer** para deduplicación
- ✅ **Documentación completa** del problema y soluciones

### **Problemas Pendientes:**
- ⚠️ **Error de lock** de Lucene impide indexación
- ⚠️ **Concurrencia** entre múltiples IndexWriter
- ⚠️ **Duplicados persistentes** en el índice

### **Próximos Pasos:**
1. Implementar solución de concurrencia en `BulkIndexerService`
2. Limpiar índice completamente
3. Reindexar con configuración correcta
4. Verificar que no hay duplicados

## 📚 Referencias

### **Documentación de Lucene:**
- [IndexWriter Configuration](https://lucene.apache.org/core/9_6_0/core/org/apache/lucene/index/IndexWriterConfig.html)
- [Concurrent Indexing](https://lucene.apache.org/core/9_6_0/core/org/apache/lucene/index/IndexWriter.html)
- [Lock Management](https://lucene.apache.org/core/9_6_0/core/org/apache/lucene/store/LockFactory.html)

### **Mejores Prácticas:**
- Usar un solo `IndexWriter` por índice
- Implementar sincronización adecuada
- Configurar locks apropiadamente
- Usar `updateDocument` para deduplicación

## 🔧 **SOLUCIÓN FINAL IMPLEMENTADA**

### **Problema de Opciones de Indexación:**
Durante la implementación apareció un nuevo error:
```
cannot change field "filename" from index options=DOCS_AND_FREQS_AND_POSITIONS to inconsistent index options=DOCS
```

Este error ocurre porque Lucene no permite cambiar las opciones de indexación de un campo existente. La solución es limpiar completamente el índice antes de cambiar la configuración de campos.

### **Solución Completa Implementada:**

#### **1. Modificación del BulkIndexerService:**
```java
@Service
public class BulkIndexerService {
    
    public BulkIndexResult indexFiles(List<File> pdfFiles) throws IOException {
        // Usar un solo IndexWriter para toda la operación
        try (Directory dir = FSDirectory.open(indexPath);
             IndexWriter writer = new IndexWriter(dir, createIndexWriterConfig())) {
            
            for (File pdfFile : pdfFiles) {
                // Crear documento con deduplicación usando el mismo writer
                Document document = createDocument(pdfFile, content);
                Term term = new Term("filename", pdfFile.getName());
                writer.updateDocument(term, document);
            }
            
            // Commit explícito para asegurar que todos los cambios se escriban
            writer.commit();
        }
    }
    
    private IndexWriterConfig createIndexWriterConfig() {
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return config;
    }
}
```

#### **2. Configuración de Campos Optimizada:**
```java
private Document createDocument(File pdfFile, String content) {
    Document document = new Document();
    
    // Usar StringField para campos únicos (evita duplicados)
    document.add(new StringField("filename", pdfFile.getName(), TextField.Store.YES));
    document.add(new StringField("filepath", pdfFile.getAbsolutePath(), TextField.Store.YES));
    document.add(new TextField("content", content, TextField.Store.YES));
    
    // Metadatos con StringField para consistencia
    document.add(new StringField("size", String.valueOf(pdfFile.length()), TextField.Store.YES));
    document.add(new StringField("lastModified", String.valueOf(pdfFile.lastModified()), TextField.Store.YES));
    document.add(new StringField("type", "pdf", TextField.Store.YES));
    document.add(new StringField("language", "es", TextField.Store.YES));
    
    return document;
}
```

## 📊 **RESULTADOS FINALES**

### **Antes de la Solución:**
- **2,210 documentos** indexados (duplicados)
- **Error de lock** de Lucene
- **Error de opciones** de indexación
- **Resultados duplicados** en búsquedas

### **Después de la Solución:**
- **104 documentos** indexados (exactamente el número de archivos PDF)
- **Sin errores de lock** durante la indexación
- **Sin errores de opciones** de indexación
- **Sin duplicados** en las búsquedas
- **Ratio 1:1** perfecto

### **Verificación de Resultados:**
```bash
# Estadísticas del índice
curl "http://localhost:8080/api/search/stats"
# Resultado: 104 documentos

# Prueba de búsqueda sin duplicados
curl "http://localhost:8080/api/search/documents?query=IVA&limit=5"
# Resultado: 5 documentos únicos
```

## 🛠️ **PASOS PARA RESOLVER COMPLETAMENTE**

### **1. Limpiar Índice Completamente:**
```bash
# Detener microservicio
pkill -f "LoadnormasApplication"

# Limpiar índice completamente
rm -rf path/to/index/*

# Reiniciar microservicio
mvn spring-boot:run -Dmaven.test.skip=true &

# Reindexar
curl -X POST "http://localhost:8080/api/bulk/index-sii-documents"
```

### **2. Verificar Solución:**
```bash
# Verificar estadísticas
curl "http://localhost:8080/api/search/stats"

# Probar búsqueda
curl "http://localhost:8080/api/search/documents?query=IVA&limit=5"

# Analizar duplicados
./analyze_duplicates.sh
```

## 🎯 **LECCIONES APRENDIDAS**

### **Problemas Identificados:**
1. **Concurrencia**: Múltiples `IndexWriter` causan locks
2. **Deduplicación**: Falta de control de duplicados
3. **Configuración de campos**: Inconsistencia en opciones de indexación
4. **Gestión de estado**: Falta de limpieza completa del índice

### **Soluciones Implementadas:**
1. **Un solo IndexWriter** por operación
2. **updateDocument** con Term-based deduplication
3. **StringField** para campos únicos
4. **Limpieza completa** del índice antes de cambios

### **Mejores Prácticas:**
- Usar `StringField` para campos únicos
- Implementar `updateDocument` para deduplicación
- Configurar `IndexWriterConfig` apropiadamente
- Limpiar índice completamente antes de cambios de esquema

---

**Estado:** ✅ **RESUELTO COMPLETAMENTE** - Todos los problemas identificados y solucionados

*Documentación actualizada el $(date) - Microservicio de Documentos Normativos SII*
