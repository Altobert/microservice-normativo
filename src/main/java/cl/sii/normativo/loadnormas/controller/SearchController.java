package cl.sii.normativo.loadnormas.controller;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    //private final String indexDir = "path/to/index";
    //appled to the same path as in LuceneIndexer.java
    //private final String indexDir = "C:\\Users\\Usuario\\Documents\\index";
    //private final String indexDir = "C:\\Users\\alberto.sanmartin\\ProyectosNormativos\\index";
    private final String indexDir = "/Users/albertosanmartin/usach-memoria-implementacion/desarrollo/proyecto-normativo-ms/microservice-normativo/normativo-indice/indice";

    @GetMapping
    public List<String> search(@RequestParam("query") String queryStr) throws Exception {
        List<String> results = new ArrayList<>();
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             DirectoryReader reader = DirectoryReader.open(dir)) {
             
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", new StandardAnalyzer());            
            Query query = parser.parse(queryStr);

            for (var hit : searcher.search(query, 10).scoreDocs) {
                try {
                    Document doc = searcher.doc(hit.doc);
                    results.add(doc.get("filename"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            /*searcher.search(query, 10).scoreDocs
                    .forEach(hit -> {
                        try {
                            Document doc = searcher.doc(hit.doc);
                            results.add(doc.get("filename"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
            });*/
        }
        return results;
    }
}
