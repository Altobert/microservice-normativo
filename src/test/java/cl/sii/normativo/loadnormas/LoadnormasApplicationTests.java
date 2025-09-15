package cl.sii.normativo.loadnormas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.TopDocs;
import org.springframework.boot.test.context.SpringBootTest;

import org.apache.lucene.store.FSDirectory;
import cl.sii.normativo.loadnormas.dto.ResponseLuceneCorpus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;


@SpringBootTest
@Slf4j
class LoadnormasApplicationTests  {

  private Directory indexDirectory;
  private StandardAnalyzer analyzer;

  private final String indexDir = "path/to/index";    

    /**
     * Método para buscar documentos en el índice de Lucene.
     * @param queryStr La cadena de consulta.
     * @return Una lista de documentos que coinciden con la consulta.
     * @throws Exception Si hay un error al abrir el índice o al realizar la búsqueda.
     */
    
    @Test
    public void search( ) throws Exception {        
        String queryStr = "iva";
        List<ResponseLuceneCorpus> responseList = new ArrayList<>();
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             DirectoryReader reader = DirectoryReader.open(dir)) {
             
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", new StandardAnalyzer());            
            Query query = parser.parse(queryStr);

            TopDocs results = searcher.search(query, 10);
            for (ScoreDoc hit : results.scoreDocs) {
                Document doc = searcher.doc(hit.doc);
                ResponseLuceneCorpus response = new ResponseLuceneCorpus();                
                response.setContent(doc.get("content"));
                responseList.add(response);
            }
            // show results
            log.info("Documentos a buscar con texto completo: "+queryStr);
            log.info("Resultados encontrados: "+results.totalHits.toString());

        } catch (IOException | ParseException e) {
            throw new Exception("Error during search operation", e);
        }
                
    }

	 @BeforeEach
    void setup() throws IOException {
        indexDirectory = new ByteBuffersDirectory(); // Use in-memory directory for testing
        analyzer = new StandardAnalyzer();

        // Populate the index with some test data
        try (IndexWriter writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer))) {
            Document doc1 = new Document();
            doc1.add(new StringField("id", "1", Field.Store.YES));
            doc1.add(new TextField("content", "El iva es importante para el pais.", Field.Store.YES));
            //doc1.add(new TextField("content", "Spring Boot is a powerful framework.", Field.Store.YES));
            writer.addDocument(doc1);

            Document doc2 = new Document();
            doc2.add(new StringField("id", "2", Field.Store.YES));
            //doc2.add(new TextField("content", "Lucene provides search capabilities.", Field.Store.YES));
            doc2.add(new TextField("content", "El servicio de impuestos internos es una institucion importante para chile.", Field.Store.YES));
            writer.addDocument(doc2);
        }
    }

    @Test
    void testSearchByKeyword() throws IOException, ParseException {
      
        try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", analyzer);

            Query query = parser.parse("iva");
            ScoreDoc[] hits = searcher.search(query, 10).scoreDocs;
            log.info("Number of hits: {}", hits.length);
            //assertEquals(1, hits.length);
            //assertEquals("1", searcher.doc(hits[0].doc).get("id"));
            assertEquals("1", searcher.storedFields().document(hits[0].doc).get("id"));

            query = parser.parse("servicio de impuestos internos");
            hits = searcher.search(query, 10).scoreDocs;
            assertEquals(1, hits.length);
            //assertEquals("2", searcher.storedFieldoc(hits[0].doc).get("id"));
            assertEquals("2", searcher.storedFields().document(hits[0].doc).get("id"));

            query = parser.parse("nonexistent");
            hits = searcher.search(query, 10).scoreDocs;
            assertEquals(0, hits.length);
        }
    }

      @Test
    void testIndexingAndRetrieval() throws IOException, ParseException {
        // Add a new document and verify retrieval
        try (IndexWriter writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer))) {
            Document doc3 = new Document();
            doc3.add(new StringField("id", "3", Field.Store.YES));
            doc3.add(new TextField("content", "New document for testing.", Field.Store.YES));
            writer.addDocument(doc3);
        }

        try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", analyzer);

            Query query = parser.parse("testing");
            ScoreDoc[] hits = searcher.search(query, 10).scoreDocs;
            assertEquals(1, hits.length);
            assertEquals("3", searcher.storedFields().document(hits[0].doc).get("id"));
        }
    }
	

}
