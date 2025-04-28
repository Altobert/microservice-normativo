package cl.sii.normativo.loadnormas.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cl.sii.normativo.loadnormas.services.LuceneIndexer;
import cl.sii.normativo.loadnormas.services.PDFTextExtractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    // Dependencia a la clase LuceneIndexer para indexar documentos
    // y realizar búsquedas
    @Autowired
    private final LuceneIndexer luceneIndexer;

    public DocumentController(LuceneIndexer luceneIndexer) {
        this.luceneIndexer = luceneIndexer;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPDF(@RequestParam("file") MultipartFile file) {
        try {
            // Extraer texto del PDF
            String content = PDFTextExtractor.extractText(file.getInputStream());
            
            // Indexar el contenido del PDF
            luceneIndexer.indexFile(file.getOriginalFilename(), content);

            return ResponseEntity.ok("Documento indexado exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al indexar el documento: " + e.getMessage());
        }
    }
    
}

   

