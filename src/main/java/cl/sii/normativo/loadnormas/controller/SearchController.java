package cl.sii.normativo.loadnormas.controller;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

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
    private final String indexDir = "path/to/index";

    @GetMapping
    public List<String> search(@RequestParam("query") String queryStr) throws Exception {
        List<String> results = new ArrayList<>();
        
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             DirectoryReader reader = DirectoryReader.open(dir)) {
             
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", new StandardAnalyzer());            
            Query query = parser.parse(queryStr);
            
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
