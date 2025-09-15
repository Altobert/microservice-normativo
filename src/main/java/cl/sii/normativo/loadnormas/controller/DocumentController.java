package cl.sii.normativo.loadnormas.controller;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Gestión de Documentos", description = "API para la gestión individual de documentos normativos")
public class DocumentController {
    
    // Dependencia a la clase LuceneIndexer para indexar documentos
    // y realizar búsquedas
    @Autowired
    private final LuceneIndexer luceneIndexer;

    public DocumentController(LuceneIndexer luceneIndexer) {
        this.luceneIndexer = luceneIndexer;
    }

    @Operation(
        summary = "Subir y indexar documento PDF",
        description = "Permite subir un documento PDF individual y lo indexa en el sistema de búsqueda de Lucene"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Documento indexado exitosamente",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(value = "Documento indexado exitosamente.")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error al procesar el documento",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(value = "Error al indexar el documento: [mensaje de error]")
            )
        )
    })
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPDF(
        @Parameter(
            description = "Archivo PDF a indexar",
            required = true,
            content = @Content(mediaType = "multipart/form-data")
        )
        @RequestParam("file") MultipartFile file) {
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

   

