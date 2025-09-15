package cl.sii.normativo.loadnormas.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI 3 (Swagger) para el microservicio de documentos normativos
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservicio de Documentos Normativos SII")
                        .description("""
                                API REST para la gestión y búsqueda de documentos normativos del Servicio de Impuestos Internos (SII).
                                
                                ## Funcionalidades principales:
                                - **Indexación de documentos**: Carga y procesamiento de documentos PDF
                                - **Búsqueda de texto completo**: Búsqueda avanzada en el contenido de los documentos
                                - **Búsqueda por criterios**: Filtrado por año, ID de documento, etc.
                                - **Estadísticas del índice**: Información sobre documentos indexados
                                - **Indexación masiva**: Procesamiento de múltiples documentos
                                
                                ## Tecnologías utilizadas:
                                - Spring Boot 3.4.5
                                - Apache Lucene 9.6.0 (índice de búsqueda)
                                - Apache PDFBox 2.0.25 (procesamiento de PDF)
                                - OpenAPI 3 (documentación)
                                
                                ## Endpoints disponibles:
                                - `/api/documents/*` - Gestión de documentos individuales
                                - `/api/search/*` - Búsqueda y consultas
                                - `/api/bulk/*` - Operaciones masivas
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo SII")
                                .email("desarrollo@sii.cl")
                                .url("https://www.sii.cl"))
                        .license(new License()
                                .name("Licencia Pública")
                                .url("https://www.sii.cl/licencia")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("https://api-normativo.sii.cl")
                                .description("Servidor de producción (ejemplo)")
                ));
    }
}
