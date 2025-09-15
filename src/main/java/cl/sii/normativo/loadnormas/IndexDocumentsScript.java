package cl.sii.normativo.loadnormas;

import cl.sii.normativo.loadnormas.services.BulkIndexerService;
import cl.sii.normativo.loadnormas.services.PDFTextExtractor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.List;

@SpringBootApplication
public class IndexDocumentsScript {
    
    public static void main(String[] args) {
        SpringApplication.run(IndexDocumentsScript.class, args);
    }
    
    @Bean
    public CommandLineRunner indexDocuments(BulkIndexerService bulkIndexerService) {
        return args -> {
            System.out.println("🚀 Iniciando indexación de documentos SII...");
            
            // Directorio por defecto
            String documentsPath = "/Users/albertosanmartin/usach-memoria-implementacion/proyectos-normativos/Normas_Instrucciones_SII";
            
            // Verificar si se proporcionó un directorio específico
            if (args.length > 0) {
                documentsPath = args[0];
            }
            
            try {
                BulkIndexerService.BulkIndexResult result = bulkIndexerService.indexDirectory(documentsPath);
                
                System.out.println("\n📊 RESUMEN DE INDEXACIÓN:");
                System.out.println("==========================");
                System.out.println("📁 Directorio: " + documentsPath);
                System.out.println("✅ Documentos indexados exitosamente: " + result.getSuccessCount());
                System.out.println("❌ Documentos con errores: " + result.getErrorCount());
                System.out.println("📄 Total procesados: " + (result.getSuccessCount() + result.getErrorCount()));
                
                if (!result.getErrors().isEmpty()) {
                    System.out.println("\n❌ ERRORES ENCONTRADOS:");
                    System.out.println("======================");
                    for (String error : result.getErrors()) {
                        System.out.println(error);
                    }
                }
                
                System.out.println("\n🎉 ¡Indexación completada!");
                System.out.println("💡 Ahora puedes buscar documentos usando el endpoint /api/documents/search");
                
            } catch (Exception e) {
                System.err.println("❌ Error durante la indexación: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
