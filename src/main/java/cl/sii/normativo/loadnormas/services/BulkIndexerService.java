package cl.sii.normativo.loadnormas.services;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BulkIndexerService {
    
    @Value("${lucene.index.directory:path/to/index}")
    private String indexDir;
    
    private final PDFTextExtractor pdfTextExtractor;
    private final LuceneIndexer luceneIndexer;
    
    public BulkIndexerService(PDFTextExtractor pdfTextExtractor, LuceneIndexer luceneIndexer) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.luceneIndexer = luceneIndexer;
    }
    
    /**
     * Indexa todos los documentos PDF de un directorio
     */
    public BulkIndexResult indexDirectory(String directoryPath) throws IOException {
        Path dirPath = Paths.get(directoryPath);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw new IllegalArgumentException("El directorio no existe: " + directoryPath);
        }
        
        List<File> pdfFiles = findPDFFiles(dirPath.toFile());
        System.out.println("📁 Encontrados " + pdfFiles.size() + " archivos PDF para indexar");
        
        return indexFiles(pdfFiles);
    }
    
    /**
     * Indexa una lista específica de archivos PDF
     */
    public BulkIndexResult indexFiles(List<File> pdfFiles) throws IOException {
        // Crear directorio de índice si no existe
        Path indexPath = Paths.get(indexDir);
        if (!Files.exists(indexPath)) {
            Files.createDirectories(indexPath);
        }
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<String> errors = new ArrayList<>();
        
        try (Directory dir = FSDirectory.open(indexPath);
             IndexWriter writer = new IndexWriter(dir, createIndexWriterConfig())) {
            
            for (File pdfFile : pdfFiles) {
                try {
                    System.out.println("📄 Procesando: " + pdfFile.getName());
                    
                    // Extraer texto del PDF
                    String content = pdfTextExtractor.extractText(new java.io.FileInputStream(pdfFile));
                    
                    // Crear documento con deduplicación usando el mismo writer
                    Document document = createDocument(pdfFile, content);
                    Term term = new Term("filename", pdfFile.getName());
                    writer.updateDocument(term, document);
                    
                    successCount.incrementAndGet();
                    System.out.println("✅ Indexado exitoso: " + pdfFile.getName());
                    
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    String error = "❌ Error en " + pdfFile.getName() + ": " + e.getMessage();
                    errors.add(error);
                    System.out.println(error);
                }
            }
            
            // Commit de todos los cambios
            writer.commit();
        }
        
        return new BulkIndexResult(successCount.get(), errorCount.get(), errors);
    }
    
    /**
     * Busca recursivamente todos los archivos PDF en un directorio
     */
    private List<File> findPDFFiles(File directory) {
        List<File> pdfFiles = new ArrayList<>();
        
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // Búsqueda recursiva en subdirectorios
                        pdfFiles.addAll(findPDFFiles(file));
                    } else if (file.getName().toLowerCase().endsWith(".pdf")) {
                        pdfFiles.add(file);
                    }
                }
            }
        }
        
        return pdfFiles;
    }
    
    /**
     * Crea un documento de Lucene con metadatos del archivo PDF
     */
    private Document createDocument(File pdfFile, String content) {
        Document document = new Document();
        
        // Información básica del archivo - usar StringField para evitar duplicados
        document.add(new StringField("filename", pdfFile.getName(), TextField.Store.YES));
        document.add(new StringField("filepath", pdfFile.getAbsolutePath(), TextField.Store.YES));
        document.add(new TextField("content", content, TextField.Store.YES));
        
        // Metadatos adicionales
        document.add(new StringField("size", String.valueOf(pdfFile.length()), TextField.Store.YES));
        document.add(new StringField("lastModified", String.valueOf(pdfFile.lastModified()), TextField.Store.YES));
        document.add(new StringField("type", "pdf", TextField.Store.YES));
        document.add(new StringField("language", "es", TextField.Store.YES));
        document.add(new StringField("createdDate", String.valueOf(System.currentTimeMillis()), TextField.Store.YES));
        
        // Extraer año del directorio padre si es posible
        String year = extractYearFromPath(pdfFile.getAbsolutePath());
        if (year != null) {
            document.add(new StringField("year", year, TextField.Store.YES));
        }
        
        // Extraer ID del nombre del archivo si es posible
        String documentId = extractDocumentId(pdfFile.getName());
        if (documentId != null) {
            document.add(new StringField("documentId", documentId, TextField.Store.YES));
        }
        
        // Generar título del documento
        String title = generateTitle(pdfFile.getName());
        document.add(new TextField("title", title, TextField.Store.YES));
        
        return document;
    }
    
    /**
     * Extrae el año del path del archivo
     */
    private String extractYearFromPath(String filePath) {
        // Buscar patrones como /2024/ o /2023/ en el path
        String[] pathParts = filePath.split("/");
        for (String part : pathParts) {
            if (part.matches("\\d{4}")) {
                return part;
            }
        }
        return null;
    }
    
    /**
     * Extrae el ID del documento del nombre del archivo
     */
    private String extractDocumentId(String fileName) {
        // Buscar patrones como ID2922_ o ID2942_
        if (fileName.startsWith("ID") && fileName.contains("_")) {
            int underscoreIndex = fileName.indexOf("_");
            if (underscoreIndex > 2) {
                return fileName.substring(0, underscoreIndex);
            }
        }
        return null;
    }
    
    /**
     * Genera un título legible del nombre del archivo
     */
    private String generateTitle(String fileName) {
        // Remover extensión
        String title = fileName.replaceAll("\\.pdf$", "");
        
        // Remover ID del inicio si existe
        if (title.startsWith("ID") && title.contains("_")) {
            int underscoreIndex = title.indexOf("_");
            if (underscoreIndex > 2) {
                title = title.substring(underscoreIndex + 1);
            }
        }
        
        // Reemplazar guiones bajos con espacios
        title = title.replace("_", " ");
        
        return title;
    }
    
    /**
     * Clase para almacenar resultados de indexación masiva
     */
    public static class BulkIndexResult {
        private final int successCount;
        private final int errorCount;
        private final List<String> errors;
        
        public BulkIndexResult(int successCount, int errorCount, List<String> errors) {
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.errors = errors;
        }
        
        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public List<String> getErrors() { return errors; }
        
        @Override
        public String toString() {
            return String.format("Indexación completada: %d exitosos, %d errores", successCount, errorCount);
        }
    }
    
    /**
     * Crea una configuración optimizada para IndexWriter
     */
    private IndexWriterConfig createIndexWriterConfig() {
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return config;
    }
}
