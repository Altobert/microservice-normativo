package cl.sii.normativo.loadnormas.services;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;

@Service
public class LuceneIndexer {
    
    private final String indexDir = "path/to/index";

    public void indexFile(String fileName, String content) throws IOException {
        try (Directory dir = FSDirectory.open(Paths.get(indexDir));
             IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            
            Document document = new Document();
            document.add(new TextField("filename", fileName, TextField.Store.YES));
            document.add(new TextField("content", content, TextField.Store.YES));
            document.add(new TextField("path", indexDir, TextField.Store.YES));
            document.add(new TextField("lastModified", String.valueOf(System.currentTimeMillis()), TextField.Store.YES));
            document.add(new TextField("size", String.valueOf(content.length()), TextField.Store.YES));
            document.add(new TextField("type", "pdf", TextField.Store.YES)); // Assuming all files are PDFs            
            document.add(new TextField("title", fileName, TextField.Store.YES)); // Placeholder for title
            document.add(new TextField("description", "No description available", TextField.Store.YES)); // Placeholder for description
            document.add(new TextField("keywords", "none", TextField.Store.YES)); // Placeholder for keywords
            document.add(new TextField("language", "es", TextField.Store.YES)); // Assuming Spanish as default language
            document.add(new TextField("createdDate", String.valueOf(System.currentTimeMillis()), TextField.Store.YES)); // Placeholder for created date
            // Add other fields as necessary
            
            writer.addDocument(document);
        }
    }
}
