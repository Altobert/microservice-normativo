package cl.sii.normativo.loadnormas.services;

import org.springframework.stereotype.Service;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.JaccardSimilarity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Map;
import java.util.HashMap;

/**
 * Servicio para generar embeddings de texto usando técnicas de similitud.
 * 
 * Este servicio proporciona:
 * - Generación de embeddings basados en características del texto
 * - Cache de embeddings para evitar regeneración
 * - Cálculo de similitud usando técnicas de texto
 * - Fallback inteligente para diferentes tipos de consultas
 */
@Service
public class EmbeddingService {

    // Cache de embeddings para evitar regeneración
    private final ConcurrentMap<String, Map<String, Double>> embeddingCache = new ConcurrentHashMap<>();
    
    // Utilidades de similitud
    private final CosineDistance cosineDistance = new CosineDistance();
    private final JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();

    /**
     * Genera un embedding para el texto dado usando características del texto.
     * 
     * @param text El texto para generar el embedding
     * @return El vector de embedding como mapa de características
     */
    public Map<String, Double> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new HashMap<>();
        }

        // Verificar cache primero
        String cacheKey = text.trim().toLowerCase();
        Map<String, Double> cachedEmbedding = embeddingCache.get(cacheKey);
        if (cachedEmbedding != null) {
            return cachedEmbedding;
        }

        // Generar embedding basado en características del texto
        Map<String, Double> embedding = generateTextFeatures(text);
        
        // Guardar en cache
        embeddingCache.put(cacheKey, embedding);
        
        return embedding;
    }

    /**
     * Genera características del texto para simular embeddings.
     * 
     * @param text El texto para generar características
     * @return Mapa de características del texto
     */
    private Map<String, Double> generateTextFeatures(String text) {
        Map<String, Double> features = new HashMap<>();
        
        // Características básicas del texto
        features.put("length", (double) text.length());
        features.put("word_count", (double) text.split("\\s+").length);
        features.put("char_diversity", calculateCharDiversity(text));
        features.put("avg_word_length", calculateAvgWordLength(text));
        
        // Características semánticas simuladas
        features.put("legal_terms_score", calculateLegalTermsScore(text));
        features.put("tax_terms_score", calculateTaxTermsScore(text));
        features.put("formality_score", calculateFormalityScore(text));
        
        // Características de frecuencia de palabras clave
        String[] keywords = {"impuesto", "iva", "renta", "tributario", "fiscal", "contribuyente", "declaración"};
        for (String keyword : keywords) {
            features.put("keyword_" + keyword, calculateKeywordFrequency(text, keyword));
        }
        
        return features;
    }

    /**
     * Calcula la diversidad de caracteres en el texto.
     */
    private double calculateCharDiversity(String text) {
        if (text.isEmpty()) return 0.0;
        
        long uniqueChars = text.toLowerCase().chars().distinct().count();
        return (double) uniqueChars / text.length();
    }

    /**
     * Calcula la longitud promedio de las palabras.
     */
    private double calculateAvgWordLength(String text) {
        String[] words = text.split("\\s+");
        if (words.length == 0) return 0.0;
        
        int totalLength = 0;
        for (String word : words) {
            totalLength += word.length();
        }
        
        return (double) totalLength / words.length;
    }

    /**
     * Calcula el score de términos legales.
     */
    private double calculateLegalTermsScore(String text) {
        String[] legalTerms = {"ley", "decreto", "resolución", "circular", "norma", "reglamento", "código"};
        return calculateTermsScore(text, legalTerms);
    }

    /**
     * Calcula el score de términos tributarios.
     */
    private double calculateTaxTermsScore(String text) {
        String[] taxTerms = {"impuesto", "iva", "renta", "tributario", "fiscal", "contribuyente", "declaración", "sii"};
        return calculateTermsScore(text, taxTerms);
    }

    /**
     * Calcula el score de formalidad del texto.
     */
    private double calculateFormalityScore(String text) {
        String[] formalWords = {"por tanto", "en consecuencia", "de conformidad", "según", "conforme", "establece"};
        return calculateTermsScore(text, formalWords);
    }

    /**
     * Calcula el score de términos específicos.
     */
    private double calculateTermsScore(String text, String[] terms) {
        String lowerText = text.toLowerCase();
        int matches = 0;
        
        for (String term : terms) {
            if (lowerText.contains(term.toLowerCase())) {
                matches++;
            }
        }
        
        return (double) matches / terms.length;
    }

    /**
     * Calcula la frecuencia de una palabra clave específica.
     */
    private double calculateKeywordFrequency(String text, String keyword) {
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        
        int count = 0;
        int index = 0;
        
        while ((index = lowerText.indexOf(lowerKeyword, index)) != -1) {
            count++;
            index += lowerKeyword.length();
        }
        
        return (double) count / text.split("\\s+").length;
    }

    /**
     * Genera embeddings de forma asíncrona para múltiples textos.
     * 
     * @param texts Lista de textos para generar embeddings
     * @return CompletableFuture con la lista de embeddings
     */
    public CompletableFuture<List<Map<String, Double>>> generateEmbeddingsAsync(List<String> texts) {
        return CompletableFuture.supplyAsync(() -> {
            return texts.stream()
                    .map(this::generateEmbedding)
                    .toList();
        });
    }

    /**
     * Calcula la similitud coseno entre dos embeddings.
     * 
     * @param embedding1 Primer embedding
     * @param embedding2 Segundo embedding
     * @return Similitud coseno (0-1, donde 1 es idéntico)
     */
    public double calculateCosineSimilarity(Map<String, Double> embedding1, Map<String, Double> embedding2) {
        if (embedding1.isEmpty() || embedding2.isEmpty()) {
            return 0.0;
        }

        // Obtener todas las claves únicas
        Map<String, Double> allKeys = new HashMap<>(embedding1);
        allKeys.putAll(embedding2);

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : allKeys.keySet()) {
            double val1 = embedding1.getOrDefault(key, 0.0);
            double val2 = embedding2.getOrDefault(key, 0.0);
            
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Calcula la similitud usando distancia coseno de Apache Commons Text.
     * 
     * @param text1 Primer texto
     * @param text2 Segundo texto
     * @return Similitud (0-1, donde 1 es idéntico)
     */
    public double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        try {
            // Usar Jaccard similarity como alternativa
            return jaccardSimilarity.apply(text1, text2);
        } catch (Exception e) {
            // Fallback a similitud simple
            return calculateSimpleSimilarity(text1, text2);
        }
    }

    /**
     * Calcula similitud simple basada en palabras comunes.
     */
    private double calculateSimpleSimilarity(String text1, String text2) {
        String[] words1 = text1.toLowerCase().split("\\s+");
        String[] words2 = text2.toLowerCase().split("\\s+");
        
        int commonWords = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2)) {
                    commonWords++;
                    break;
                }
            }
        }
        
        int totalWords = words1.length + words2.length;
        return totalWords > 0 ? (double) (2 * commonWords) / totalWords : 0.0;
    }

    /**
     * Obtiene el tamaño del cache de embeddings.
     * 
     * @return Número de embeddings en cache
     */
    public int getCacheSize() {
        return embeddingCache.size();
    }

    /**
     * Limpia el cache de embeddings.
     */
    public void clearCache() {
        embeddingCache.clear();
    }

    /**
     * Verifica si el servicio está disponible.
     * 
     * @return true si el servicio está disponible
     */
    public boolean isAvailable() {
        return true; // Siempre disponible con implementación simplificada
    }
}