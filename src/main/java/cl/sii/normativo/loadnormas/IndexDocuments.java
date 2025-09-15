package cl.sii.normativo.loadnormas;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IndexDocuments {
    
    private static final String INDEX_DIR = "path/to/index";
    private static final String DOCUMENTS_DIR = "/Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII";
    
    public static void main(String[] args) {
        System.out.println("🚀 Iniciando indexación de documentos SII...");
        
        try {
            // Crear directorio de índice si no existe
            Path indexPath = Paths.get(INDEX_DIR);
            if (!Files.exists(indexPath)) {
                Files.createDirectories(indexPath);
                System.out.println("📁 Directorio de índice creado: " + INDEX_DIR);
            }
            
            // Buscar archivos PDF
            List<File> pdfFiles = findPDFFiles(new File(DOCUMENTS_DIR));
            System.out.println("📄 Encontrados " + pdfFiles.size() + " archivos PDF para indexar");
            
            // Indexar archivos
            indexFiles(pdfFiles);
            
        } catch (Exception e) {
            System.err.println("❌ Error durante la indexación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void indexFiles(List<File> pdfFiles) throws IOException {
        int successCount = 0;
        int errorCount = 0;
        
        try (Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
             IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            
            for (File pdfFile : pdfFiles) {
                try {
                    System.out.println("📄 Procesando: " + pdfFile.getName());
                    
                    // Extraer texto del PDF
                    String content = extractTextFromPDF(pdfFile);
                    
                    // Crear documento de Lucene
                    Document document = createDocument(pdfFile, content);
                    
                    // Agregar al índice
                    writer.addDocument(document);
                    
                    successCount++;
                    System.out.println("✅ Indexado exitoso: " + pdfFile.getName());
                    
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("❌ Error en " + pdfFile.getName() + ": " + e.getMessage());
                }
            }
            
            // Commit de todos los cambios
            writer.commit();
        }
        
        System.out.println("\n📊 RESUMEN DE INDEXACIÓN:");
        System.out.println("==========================");
        System.out.println("✅ Documentos indexados exitosamente: " + successCount);
        System.out.println("❌ Documentos con errores: " + errorCount);
        System.out.println("📄 Total procesados: " + (successCount + errorCount));
        System.out.println("\n🎉 ¡Indexación completada!");
    }
    
    private static String extractTextFromPDF(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    private static Document createDocument(File pdfFile, String content) {
        Document document = new Document();
        
        // Información básica del archivo
        document.add(new TextField("filename", pdfFile.getName(), TextField.Store.YES));
        document.add(new TextField("filepath", pdfFile.getAbsolutePath(), TextField.Store.YES));
        document.add(new TextField("content", content, TextField.Store.YES));
        
        // Metadatos adicionales
        document.add(new TextField("size", String.valueOf(pdfFile.length()), TextField.Store.YES));
        document.add(new TextField("lastModified", String.valueOf(pdfFile.lastModified()), TextField.Store.YES));
        document.add(new TextField("type", "pdf", TextField.Store.YES));
        document.add(new TextField("language", "es", TextField.Store.YES));
        document.add(new TextField("createdDate", String.valueOf(System.currentTimeMillis()), TextField.Store.YES));
        
        // Extraer año del directorio padre si es posible
        String year = extractYearFromPath(pdfFile.getAbsolutePath());
        if (year != null) {
            document.add(new TextField("year", year, TextField.Store.YES));
        }
        
        // Extraer ID del nombre del archivo si es posible
        String documentId = extractDocumentId(pdfFile.getName());
        if (documentId != null) {
            document.add(new TextField("documentId", documentId, TextField.Store.YES));
        }
        
        // Generar título del documento
        String title = generateTitle(pdfFile.getName());
        document.add(new TextField("title", title, TextField.Store.YES));
        
        return document;
    }
    
    private static List<File> findPDFFiles(File directory) {
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
    
    private static String extractYearFromPath(String filePath) {
        String[] pathParts = filePath.split("/");
        for (String part : pathParts) {
            if (part.matches("\\d{4}")) {
                return part;
            }
        }
        return null;
    }
    
    private static String extractDocumentId(String fileName) {
        if (fileName.startsWith("ID") && fileName.contains("_")) {
            int underscoreIndex = fileName.indexOf("_");
            if (underscoreIndex > 2) {
                return fileName.substring(0, underscoreIndex);
            }
        }
        return null;
    }
    
    private static String generateTitle(String fileName) {
        String title = fileName.replaceAll("\\.pdf$", "");
        
        if (title.startsWith("ID") && title.contains("_")) {
            int underscoreIndex = title.indexOf("_");
            if (underscoreIndex > 2) {
                title = title.substring(underscoreIndex + 1);
            }
        }
        
        title = title.replace("_", " ");
        return title;
    }
}
