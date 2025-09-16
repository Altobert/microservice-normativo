package cl.sii.normativo.loadnormas.services;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Servicio para indexación vectorial de documentos usando Lucene Vector.
 * 
 * Este servicio permite:
 * - Indexar documentos con embeddings vectoriales
 * - Mantener índices híbridos (texto + vectores)
 * - Optimizar la búsqueda vectorial
 */
@Service
public class VectorIndexerService {

    @Autowired
    private EmbeddingService embeddingService;

    @Value("${lucene.index.directory:path/to/index}")
    private String indexDir;

    private static final String VECTOR_FIELD = "vector";
    private static final String CONTENT_FIELD = "content";
    private static final String FILENAME_FIELD = "filename";
    private static final String DOCUMENT_ID_FIELD = "documentId";
    private static final String YEAR_FIELD = "year";
    private static final String TITLE_FIELD = "title";

    /**
     * Indexa un documento con su embedding vectorial.
     * 
     * @param documentId ID único del documento
     * @param filename Nombre del archivo
     * @param title Título del documento
     * @param content Contenido del documento
     * @param year Año del documento
     * @throws IOException Si hay error en la indexación
     */
    public void indexDocumentWithVector(String documentId, String filename, String title, 
                                      String content, String year) throws IOException {
        
        System.out.println("Indexando documento con vector: " + documentId);
        
        // Generar embedding para el contenido
        Map<String, Double> embedding = embeddingService.generateEmbedding(content);
        
        // Crear documento de Lucene
        Document doc = new Document();
        
        // Campos tradicionales
        doc.add(new StringField(DOCUMENT_ID_FIELD, documentId, Field.Store.YES));
        doc.add(new StringField(FILENAME_FIELD, filename, Field.Store.YES));
        doc.add(new TextField(TITLE_FIELD, title, Field.Store.YES));
        doc.add(new TextField(CONTENT_FIELD, content, Field.Store.YES));
        doc.add(new StringField(YEAR_FIELD, year, Field.Store.YES));
        
        // Campo vectorial como texto serializado
        doc.add(new TextField(VECTOR_FIELD, serializeEmbedding(embedding), Field.Store.YES));
        
        // Indexar el documento
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            
            writer.addDocument(doc);
            writer.commit();
            
            System.out.println("Documento indexado exitosamente: " + documentId);
        }
    }

    /**
     * Indexa múltiples documentos de forma batch.
     * 
     * @param documents Lista de documentos a indexar
     * @throws IOException Si hay error en la indexación
     */
    public void indexDocumentsBatch(List<Map<String, String>> documents) throws IOException {
        System.out.println("Iniciando indexación batch de " + documents.size() + " documentos");
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            
            for (Map<String, String> docData : documents) {
                String documentId = docData.get("documentId");
                String filename = docData.get("filename");
                String title = docData.get("title");
                String content = docData.get("content");
                String year = docData.get("year");
                
                if (content != null && !content.trim().isEmpty()) {
                    // Generar embedding
                    Map<String, Double> embedding = embeddingService.generateEmbedding(content);
                    
                    // Crear documento
                    Document doc = new Document();
                    doc.add(new StringField(DOCUMENT_ID_FIELD, documentId, Field.Store.YES));
                    doc.add(new StringField(FILENAME_FIELD, filename, Field.Store.YES));
                    doc.add(new TextField(TITLE_FIELD, title, Field.Store.YES));
                    doc.add(new TextField(CONTENT_FIELD, content, Field.Store.YES));
                    doc.add(new StringField(YEAR_FIELD, year, Field.Store.YES));
                    doc.add(new TextField(VECTOR_FIELD, serializeEmbedding(embedding), Field.Store.YES));
                    
                    writer.addDocument(doc);
                }
            }
            
            writer.commit();
            System.out.println("Indexación batch completada exitosamente");
        }
    }

    /**
     * Actualiza el embedding de un documento existente.
     * 
     * @param documentId ID del documento a actualizar
     * @param newContent Nuevo contenido del documento
     * @throws IOException Si hay error en la actualización
     */
    public void updateDocumentVector(String documentId, String newContent) throws IOException {
        System.out.println("Actualizando vector del documento: " + documentId);
        
        // Generar nuevo embedding
        Map<String, Double> newEmbedding = embeddingService.generateEmbedding(newContent);
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            
            // Crear documento actualizado
            Document doc = new Document();
            doc.add(new StringField(DOCUMENT_ID_FIELD, documentId, Field.Store.YES));
            doc.add(new TextField(CONTENT_FIELD, newContent, Field.Store.YES));
            doc.add(new TextField(VECTOR_FIELD, serializeEmbedding(newEmbedding), Field.Store.YES));
            
            // Actualizar el documento
            writer.updateDocument(new org.apache.lucene.index.Term(DOCUMENT_ID_FIELD, documentId), doc);
            writer.commit();
            
            System.out.println("Vector del documento actualizado: " + documentId);
        }
    }

    /**
     * Elimina un documento del índice vectorial.
     * 
     * @param documentId ID del documento a eliminar
     * @throws IOException Si hay error en la eliminación
     */
    public void deleteDocument(String documentId) throws IOException {
        System.out.println("Eliminando documento del índice: " + documentId);
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            
            writer.deleteDocuments(new org.apache.lucene.index.Term(DOCUMENT_ID_FIELD, documentId));
            writer.commit();
            
            System.out.println("Documento eliminado del índice: " + documentId);
        }
    }

    /**
     * Optimiza el índice vectorial.
     * 
     * @throws IOException Si hay error en la optimización
     */
    public void optimizeIndex() throws IOException {
        System.out.println("Optimizando índice vectorial...");
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            
            writer.forceMerge(1); // Fusionar todos los segmentos en uno
            writer.commit();
            
            System.out.println("Índice vectorial optimizado exitosamente");
        }
    }

    /**
     * Obtiene estadísticas del índice vectorial.
     * 
     * @return Map con estadísticas del índice
     */
    public Map<String, Object> getIndexStats() {
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             org.apache.lucene.index.DirectoryReader reader = org.apache.lucene.index.DirectoryReader.open(dir)) {
            
            return Map.of(
                "totalDocuments", reader.numDocs(),
                "indexDirectory", indexDir,
                "hasVectors", true,
                "embeddingCacheSize", embeddingService.getCacheSize(),
                "embeddingServiceAvailable", embeddingService.isAvailable()
            );
        } catch (IOException e) {
            return Map.of(
                "error", "No se pudo obtener estadísticas del índice: " + e.getMessage(),
                "indexDirectory", indexDir
            );
        }
    }

    /**
     * Serializa un embedding a string para almacenamiento en Lucene.
     * 
     * @param embedding El embedding a serializar
     * @return String serializado del embedding
     */
    private String serializeEmbedding(Map<String, Double> embedding) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : embedding.entrySet()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * Deserializa un embedding desde string almacenado en Lucene.
     * 
     * @param serializedEmbedding El embedding serializado
     * @return Mapa del embedding
     */
    private Map<String, Double> deserializeEmbedding(String serializedEmbedding) {
        Map<String, Double> embedding = new HashMap<>();
        if (serializedEmbedding != null && !serializedEmbedding.isEmpty()) {
            String[] pairs = serializedEmbedding.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    try {
                        embedding.put(keyValue[0], Double.parseDouble(keyValue[1]));
                    } catch (NumberFormatException e) {
                        // Ignorar valores inválidos
                    }
                }
            }
        }
        return embedding;
    }
}
