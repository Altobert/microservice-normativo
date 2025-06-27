package cl.sii.normativo.loadnormas.controller;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.sii.normativo.loadnormas.dto.ResponseLuceneCorpus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api")
public class SearchController {

    private final String indexDir = "path/to/index";     

    /**
     * Método para buscar documentos en el índice de Lucene.
     * @param queryStr La cadena de consulta.
     * @return Una lista de documentos que coinciden con la consulta.
     * @throws Exception Si hay un error al abrir el índice o al realizar la búsqueda.
     */
    
    @GetMapping("/search")    
    public List<ResponseLuceneCorpus> search(@RequestParam("query") String queryStr) throws Exception {        
        List<ResponseLuceneCorpus> responseList = new ArrayList<>();
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             DirectoryReader reader = DirectoryReader.open(dir)) {
             
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", new StandardAnalyzer());            
            Query query = parser.parse(queryStr);

            /*
             * Realiza la búsqueda y obtiene los documentos que coinciden con la consulta
             * searcher.search(query, 10).scoreDocs.forEach(hit -> { try { Document doc =
             */
            ResponseLuceneCorpus response = null;
            for (var hit : searcher.search(query, 10).scoreDocs) {
                try {                    
                    Document doc = searcher.doc(hit.doc);
                    response = new ResponseLuceneCorpus();
                    response.setFilename(doc.get("filename"));
                    response.setTitle(doc.get("title"));
                    response.setContent(doc.get("content"));
                    response.setPath(doc.get("path"));
                    response.setLastModified(doc.get("lastModified"));
                    response.setSize(doc.get("size"));
                    responseList.add(response);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                        
        }    
        return responseList;
    }

        
    
    /**
     * Método para buscar documentos en el índice de Lucene.
     * @param queryStr La cadena de consulta.
     * @return Una lista de documentos que coinciden con la consulta.
     * @throws ParseException Si hay un error al analizar la consulta.
     */    
    @GetMapping("/document")
    public List<Document> searchDocumentsOnIndex(@RequestParam("queryStr") String queryStr ) throws ParseException {
        try {
            
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            IndexReader indexReader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(indexReader);

            QueryParser parser = new QueryParser("content", new StandardAnalyzer());            
            Query query = parser.parse(queryStr);

            //TopDocs topDocs = searcher.search(query, 10, sort);
            TopDocs topDocs = searcher.search(query, 10, new Sort());
            //TopDocs topDocs = searcher.search(query, 10, sort);            
            // Con estos parametros se busca el documento por contenido.
            //TopDocs topDocs = searcher.search(query, 10, new Sort("content"));            
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(searcher.doc(scoreDoc.doc));
            }
            return documents;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public String buscarRutaDocumento(String id) {
        // Usás Lucene para buscar por ID u otro campo
        // y devolver la ruta física o lógica del archivo
        // Aquí podrías abrir el índice y buscar el campo 'path'
        ///Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII/2016
        //return "/ruta/a/documentos/documento_" + id + ".pdf";
        return "/Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII/2016/"+id;
    }

    @GetMapping("/document/{id}")
    public String getDocumentPath(@PathVariable String id) {
        // Aquí podrías implementar la lógica para buscar el documento por ID
        // y devolver su ruta
        return buscarRutaDocumento(id);
    }

    @GetMapping(value = "/documento/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
public ResponseEntity<byte[]> obtenerDocumento(@PathVariable String id) throws IOException {
    String ruta = buscarRutaDocumento(id);  // este método usa Lucene
    Path path = Paths.get(ruta);

    byte[] contenido = Files.readAllBytes(path);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
            .body(contenido);
    }

    @GetMapping("/documento")
    public ResponseEntity<byte[]> obtenerDocumentoPorConsulta(@RequestParam("query") String queryStr) throws Exception {
        List<ResponseLuceneCorpus> results = search(queryStr);
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Asumiendo que quieres el primer resultado
        String ruta = results.get(0).getPath();
        Path path = Paths.get(ruta);
        
        byte[] contenido = Files.readAllBytes(path);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
                .body(contenido);
    }



}
