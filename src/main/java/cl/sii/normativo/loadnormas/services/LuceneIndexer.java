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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LuceneIndexer {
    
    @Value("${lucene.index.directory:path/to/index}")
    private String indexDir;

    public void indexFile(String fileName, String content) throws IOException {
        indexFile(fileName, content, null);
    }
    
    public void indexFile(String fileName, String content, String filePath) throws IOException {
        // Validar parámetros
        if (fileName == null || content == null) {
            throw new IllegalArgumentException("fileName y content no pueden ser null");
        }
        
        // Crear directorio si no existe
        Path indexPath = Paths.get(indexDir);
        if (!Files.exists(indexPath)) {
            Files.createDirectories(indexPath);
        }
        
        try (Directory dir = FSDirectory.open(indexPath);
             IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            
            Document document = new Document();
            
            // Usar StringField para filename para evitar duplicados
            document.add(new StringField("filename", fileName, TextField.Store.YES));
            document.add(new TextField("content", content, TextField.Store.YES));
            
            // Agregar filepath si está disponible
            if (filePath != null) {
                document.add(new StringField("filepath", filePath, TextField.Store.YES));
            } else {
                document.add(new StringField("filepath", indexDir, TextField.Store.YES));
            }
            
            // Extraer año del filename si es posible
            String year = extractYearFromFilename(fileName);
            if (year != null) {
                document.add(new StringField("year", year, TextField.Store.YES));
            }
            
            // Extraer documentId del filename si es posible
            String documentId = extractDocumentIdFromFilename(fileName);
            if (documentId != null) {
                document.add(new StringField("documentId", documentId, TextField.Store.YES));
            }
            
            document.add(new StringField("lastModified", String.valueOf(System.currentTimeMillis()), TextField.Store.YES));
            document.add(new StringField("size", String.valueOf(content.length()), TextField.Store.YES));
            document.add(new StringField("type", "pdf", TextField.Store.YES));
            
            // Crear título más legible
            String title = createReadableTitle(fileName);
            document.add(new TextField("title", title, TextField.Store.YES));
            
            document.add(new TextField("description", "Documento normativo del SII", TextField.Store.YES));
            document.add(new TextField("keywords", "normativo, SII, impuestos", TextField.Store.YES));
            document.add(new StringField("language", "es", TextField.Store.YES));
            document.add(new StringField("createdDate", String.valueOf(System.currentTimeMillis()), TextField.Store.YES));
            
            // Usar updateDocument para evitar duplicados basado en filename
            Term term = new Term("filename", fileName);
            writer.updateDocument(term, document);
        }
    }
    
    // Método para pruebas que permite especificar el directorio
    public void indexFileForTesting(String fileName, String content, Directory directory) throws IOException {
        // Validar parámetros
        if (fileName == null || content == null) {
            throw new IllegalArgumentException("fileName y content no pueden ser null");
        }
        
        try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {
            Document document = new Document();
            document.add(new TextField("filename", fileName, TextField.Store.YES));
            document.add(new TextField("content", content, TextField.Store.YES));
            document.add(new TextField("path", "test-path", TextField.Store.YES));
            document.add(new TextField("lastModified", String.valueOf(System.currentTimeMillis()), TextField.Store.YES));
            document.add(new TextField("size", String.valueOf(content.length()), TextField.Store.YES));
            document.add(new TextField("type", "pdf", TextField.Store.YES));
            document.add(new TextField("title", fileName, TextField.Store.YES));
            document.add(new TextField("description", "No description available", TextField.Store.YES));
            document.add(new TextField("keywords", "none", TextField.Store.YES));
            document.add(new TextField("language", "es", TextField.Store.YES));
            document.add(new TextField("createdDate", String.valueOf(System.currentTimeMillis()), TextField.Store.YES));
            
            writer.addDocument(document);
        }
    }
    
    /**
     * Extrae el año del nombre del archivo
     */
    private String extractYearFromFilename(String fileName) {
        // Buscar patrones como "2020", "2021", etc.
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(20\\d{2})");
        java.util.regex.Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Extrae el ID del documento del nombre del archivo
     */
    private String extractDocumentIdFromFilename(String fileName) {
        // Buscar patrones como "ID123", "ID_123", etc.
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(ID\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Crea un título más legible a partir del nombre del archivo
     */
    private String createReadableTitle(String fileName) {
        // Remover extensión
        String title = fileName.replaceAll("\\.pdf$", "");
        
        // Reemplazar guiones bajos con espacios
        title = title.replaceAll("_", " ");
        
        // Capitalizar palabras
        String[] words = title.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
}
