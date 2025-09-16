package cl.sii.normativo.loadnormas.pruebascobertura;

import org.springframework.stereotype.Component;

/**
 * Ejemplo de uso de la clase EvaluationMetrics.
 * 
 * Esta clase demuestra cómo utilizar EvaluationMetrics para evaluar
 * el rendimiento de un sistema de clasificación o búsqueda.
 */
@Component
public class EvaluationMetricsExample {

    /**
     * Ejemplo básico de uso de EvaluationMetrics.
     * Simula la evaluación de un sistema de búsqueda de documentos.
     */
    public void ejemploBasico() {
        System.out.println("=== EJEMPLO BÁSICO DE EVALUATION METRICS ===");
        
        // Crear instancia de métricas
        EvaluationMetrics metrics = new EvaluationMetrics();
        
        // Simular resultados de búsqueda
        // Supongamos que buscamos documentos sobre "IVA" y tenemos estos resultados:
        
        // Documentos relevantes encontrados (True Positives)
        metrics.incrementTruePositives(); // Documento 1: "Circular sobre IVA"
        metrics.incrementTruePositives(); // Documento 2: "Instrucciones IVA"
        metrics.incrementTruePositives(); // Documento 3: "Modificaciones IVA"
        
        // Documentos no relevantes encontrados (False Positives)
        metrics.incrementFalsePositives(); // Documento 4: "Impuesto a la Renta" (no es IVA)
        
        // Documentos relevantes no encontrados (False Negatives)
        metrics.incrementFalseNegatives(); // Documento 5: "IVA Servicios Extranjeros" (no encontrado)
        
        // Documentos no relevantes no encontrados (True Negatives)
        metrics.incrementTrueNegatives(); // Documento 6: "Impuesto Verde" (correctamente no encontrado)
        metrics.incrementTrueNegatives(); // Documento 7: "Impuesto Específico" (correctamente no encontrado)
        
        // Calcular y mostrar métricas
        mostrarMetricas(metrics, "Búsqueda de documentos sobre IVA");
    }
    
    /**
     * Ejemplo de evaluación de diferentes tipos de clasificadores.
     */
    public void ejemploClasificadores() {
        System.out.println("\n=== EJEMPLO DE DIFERENTES CLASIFICADORES ===");
        
        // Clasificador Conservador (pocos false positives, muchos false negatives)
        System.out.println("\n--- Clasificador Conservador ---");
        EvaluationMetrics conservador = new EvaluationMetrics();
        conservador.incrementTruePositives();  // 1 TP
        conservador.incrementFalseNegatives(); // 1 FN
        conservador.incrementFalseNegatives(); // 2 FN
        conservador.incrementTrueNegatives();  // 1 TN
        conservador.incrementTrueNegatives();  // 2 TN
        mostrarMetricas(conservador, "Clasificador Conservador");
        
        // Clasificador Agresivo (muchos false positives, pocos false negatives)
        System.out.println("\n--- Clasificador Agresivo ---");
        EvaluationMetrics agresivo = new EvaluationMetrics();
        agresivo.incrementTruePositives();   // 1 TP
        agresivo.incrementTruePositives();   // 2 TP
        agresivo.incrementFalsePositives();  // 1 FP
        agresivo.incrementFalsePositives();  // 2 FP
        agresivo.incrementFalsePositives();  // 3 FP
        agresivo.incrementTrueNegatives();   // 1 TN
        mostrarMetricas(agresivo, "Clasificador Agresivo");
        
        // Clasificador Perfecto
        System.out.println("\n--- Clasificador Perfecto ---");
        EvaluationMetrics perfecto = new EvaluationMetrics();
        perfecto.incrementTruePositives();   // 1 TP
        perfecto.incrementTruePositives();   // 2 TP
        perfecto.incrementTrueNegatives();   // 1 TN
        perfecto.incrementTrueNegatives();   // 2 TN
        mostrarMetricas(perfecto, "Clasificador Perfecto");
    }
    
    /**
     * Ejemplo de evaluación de sistema de búsqueda con diferentes consultas.
     */
    public void ejemploBusquedaDocumentos() {
        System.out.println("\n=== EJEMPLO DE SISTEMA DE BÚSQUEDA ===");
        
        // Simular evaluación de múltiples consultas
        String[] consultas = {"IVA", "impuesto", "tributación", "servicio"};
        
        for (String consulta : consultas) {
            EvaluationMetrics metrics = simularBusqueda(consulta);
            mostrarMetricas(metrics, "Consulta: '" + consulta + "'");
        }
    }
    
    /**
     * Simula una búsqueda para una consulta específica.
     * En un sistema real, esto vendría de los resultados de Lucene.
     */
    private EvaluationMetrics simularBusqueda(String consulta) {
        EvaluationMetrics metrics = new EvaluationMetrics();
        
        // Simular resultados basados en la consulta
        switch (consulta.toLowerCase()) {
            case "iva":
                metrics.incrementTruePositives();  // 2 documentos relevantes encontrados
                metrics.incrementTruePositives();
                metrics.incrementFalsePositives(); // 1 documento no relevante
                metrics.incrementFalseNegatives(); // 1 documento relevante no encontrado
                metrics.incrementTrueNegatives();  // 2 documentos correctamente no encontrados
                metrics.incrementTrueNegatives();
                break;
                
            case "impuesto":
                metrics.incrementTruePositives();  // 3 documentos relevantes encontrados
                metrics.incrementTruePositives();
                metrics.incrementTruePositives();
                metrics.incrementFalsePositives(); // 2 documentos no relevantes
                metrics.incrementFalsePositives();
                metrics.incrementTrueNegatives();  // 1 documento correctamente no encontrado
                break;
                
            case "tributación":
                metrics.incrementTruePositives();  // 1 documento relevante encontrado
                metrics.incrementFalseNegatives(); // 2 documentos relevantes no encontrados
                metrics.incrementFalseNegatives();
                metrics.incrementTrueNegatives();  // 3 documentos correctamente no encontrados
                metrics.incrementTrueNegatives();
                metrics.incrementTrueNegatives();
                break;
                
            case "servicio":
                metrics.incrementTruePositives();  // 1 documento relevante encontrado
                metrics.incrementFalsePositives(); // 1 documento no relevante
                metrics.incrementTrueNegatives();  // 2 documentos correctamente no encontrados
                metrics.incrementTrueNegatives();
                break;
        }
        
        return metrics;
    }
    
    /**
     * Muestra las métricas calculadas de forma legible.
     */
    private void mostrarMetricas(EvaluationMetrics metrics, String titulo) {
        System.out.println("\n" + titulo + ":");
        System.out.println("  📊 Contadores:");
        System.out.println("    • True Positives (TP):  " + metrics.getTruePositives());
        System.out.println("    • False Positives (FP): " + metrics.getFalsePositives());
        System.out.println("    • False Negatives (FN):  " + metrics.getFalseNegatives());
        System.out.println("    • True Negatives (TN):   " + metrics.getTrueNegatives());
        
        System.out.println("  📈 Métricas:");
        System.out.printf("    • Precision: %.4f (%.2f%%)\n", 
                         metrics.getPrecision(), metrics.getPrecision() * 100);
        System.out.printf("    • Recall:     %.4f (%.2f%%)\n", 
                         metrics.getRecall(), metrics.getRecall() * 100);
        System.out.printf("    • F1-Score:   %.4f (%.2f%%)\n", 
                         metrics.getF1Score(), metrics.getF1Score() * 100);
        System.out.printf("    • Accuracy:   %.4f (%.2f%%)\n", 
                         metrics.getAccuracy(), metrics.getAccuracy() * 100);
        
        // Interpretación de las métricas
        System.out.println("  💡 Interpretación:");
        interpretarMetricas(metrics);
    }
    
    /**
     * Proporciona una interpretación de las métricas calculadas.
     */
    private void interpretarMetricas(EvaluationMetrics metrics) {
        double precision = metrics.getPrecision();
        double recall = metrics.getRecall();
        double f1Score = metrics.getF1Score();
        double accuracy = metrics.getAccuracy();
        
        // Interpretación de Precision
        if (precision >= 0.9) {
            System.out.println("    • Precision excelente: Muy pocos resultados irrelevantes");
        } else if (precision >= 0.7) {
            System.out.println("    • Precision buena: Pocos resultados irrelevantes");
        } else if (precision >= 0.5) {
            System.out.println("    • Precision moderada: Algunos resultados irrelevantes");
        } else {
            System.out.println("    • Precision baja: Muchos resultados irrelevantes");
        }
        
        // Interpretación de Recall
        if (recall >= 0.9) {
            System.out.println("    • Recall excelente: Encuentra casi todos los documentos relevantes");
        } else if (recall >= 0.7) {
            System.out.println("    • Recall bueno: Encuentra la mayoría de documentos relevantes");
        } else if (recall >= 0.5) {
            System.out.println("    • Recall moderado: Encuentra algunos documentos relevantes");
        } else {
            System.out.println("    • Recall bajo: Pierde muchos documentos relevantes");
        }
        
        // Interpretación de F1-Score
        if (f1Score >= 0.9) {
            System.out.println("    • F1-Score excelente: Balance perfecto entre precision y recall");
        } else if (f1Score >= 0.7) {
            System.out.println("    • F1-Score bueno: Buen balance entre precision y recall");
        } else if (f1Score >= 0.5) {
            System.out.println("    • F1-Score moderado: Balance aceptable");
        } else {
            System.out.println("    • F1-Score bajo: Necesita mejora en precision o recall");
        }
        
        // Interpretación de Accuracy
        if (accuracy >= 0.9) {
            System.out.println("    • Accuracy excelente: Sistema muy preciso");
        } else if (accuracy >= 0.7) {
            System.out.println("    • Accuracy bueno: Sistema preciso");
        } else if (accuracy >= 0.5) {
            System.out.println("    • Accuracy moderado: Sistema aceptable");
        } else {
            System.out.println("    • Accuracy bajo: Sistema necesita mejoras");
        }
    }
    
    /**
     * Método principal para ejecutar todos los ejemplos.
     */
    public void ejecutarTodosLosEjemplos() {
        ejemploBasico();
        ejemploClasificadores();
        ejemploBusquedaDocumentos();
        
        System.out.println("\n=== RESUMEN ===");
        System.out.println("✅ EvaluationMetrics permite evaluar sistemas de clasificación y búsqueda");
        System.out.println("✅ Calcula métricas estándar: Precision, Recall, F1-Score, Accuracy");
        System.out.println("✅ Maneja casos edge como división por cero");
        System.out.println("✅ Proporciona interpretación de resultados");
        System.out.println("✅ Ideal para evaluar el rendimiento de sistemas de búsqueda de documentos");
    }
}
