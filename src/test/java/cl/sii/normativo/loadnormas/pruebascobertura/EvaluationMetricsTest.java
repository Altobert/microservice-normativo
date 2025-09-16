package cl.sii.normativo.loadnormas.pruebascobertura;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias completas para la clase EvaluationMetrics.
 * 
 * Esta clase prueba todas las funcionalidades de EvaluationMetrics incluyendo:
 * - Métodos de incremento de contadores
 * - Métodos getter de contadores
 * - Cálculos de métricas (precision, recall, f1-score, accuracy)
 * - Casos edge como división por cero
 * - Escenarios de prueba realistas
 */
class EvaluationMetricsTest {

    private EvaluationMetrics evaluationMetrics;

    @BeforeEach
    void setUp() {
        evaluationMetrics = new EvaluationMetrics();
    }

    @Nested
    @DisplayName("Pruebas de Métodos de Incremento")
    class IncrementMethodsTests {

        @Test
        @DisplayName("Debería incrementar truePositives correctamente")
        void shouldIncrementTruePositives() {
            // Given
            assertEquals(0, evaluationMetrics.getTruePositives());

            // When
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTruePositives();

            // Then
            assertEquals(2, evaluationMetrics.getTruePositives());
        }

        @Test
        @DisplayName("Debería incrementar falsePositives correctamente")
        void shouldIncrementFalsePositives() {
            // Given
            assertEquals(0, evaluationMetrics.getFalsePositives());

            // When
            evaluationMetrics.incrementFalsePositives();
            evaluationMetrics.incrementFalsePositives();
            evaluationMetrics.incrementFalsePositives();

            // Then
            assertEquals(3, evaluationMetrics.getFalsePositives());
        }

        @Test
        @DisplayName("Debería incrementar falseNegatives correctamente")
        void shouldIncrementFalseNegatives() {
            // Given
            assertEquals(0, evaluationMetrics.getFalseNegatives());

            // When
            evaluationMetrics.incrementFalseNegatives();

            // Then
            assertEquals(1, evaluationMetrics.getFalseNegatives());
        }

        @Test
        @DisplayName("Debería incrementar trueNegatives correctamente")
        void shouldIncrementTrueNegatives() {
            // Given
            assertEquals(0, evaluationMetrics.getTrueNegatives());

            // When
            evaluationMetrics.incrementTrueNegatives();
            evaluationMetrics.incrementTrueNegatives();
            evaluationMetrics.incrementTrueNegatives();
            evaluationMetrics.incrementTrueNegatives();

            // Then
            assertEquals(4, evaluationMetrics.getTrueNegatives());
        }

        @Test
        @DisplayName("Debería manejar múltiples incrementos de diferentes tipos")
        void shouldHandleMultipleIncrementsOfDifferentTypes() {
            // When
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementFalsePositives();
            evaluationMetrics.incrementFalseNegatives();
            evaluationMetrics.incrementFalseNegatives();
            evaluationMetrics.incrementTrueNegatives();
            evaluationMetrics.incrementTrueNegatives();
            evaluationMetrics.incrementTrueNegatives();

            // Then
            assertEquals(2, evaluationMetrics.getTruePositives());
            assertEquals(1, evaluationMetrics.getFalsePositives());
            assertEquals(2, evaluationMetrics.getFalseNegatives());
            assertEquals(3, evaluationMetrics.getTrueNegatives());
        }
    }

    @Nested
    @DisplayName("Pruebas de Métodos Getter")
    class GetterMethodsTests {

        @Test
        @DisplayName("Debería retornar valores iniciales correctos")
        void shouldReturnCorrectInitialValues() {
            // Then
            assertEquals(0, evaluationMetrics.getTruePositives());
            assertEquals(0, evaluationMetrics.getFalsePositives());
            assertEquals(0, evaluationMetrics.getFalseNegatives());
            assertEquals(0, evaluationMetrics.getTrueNegatives());
        }

        @Test
        @DisplayName("Debería retornar valores correctos después de incrementos")
        void shouldReturnCorrectValuesAfterIncrements() {
            // Given
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementFalsePositives();
            evaluationMetrics.incrementFalseNegatives();
            evaluationMetrics.incrementTrueNegatives();

            // Then
            assertEquals(2, evaluationMetrics.getTruePositives());
            assertEquals(1, evaluationMetrics.getFalsePositives());
            assertEquals(1, evaluationMetrics.getFalseNegatives());
            assertEquals(1, evaluationMetrics.getTrueNegatives());
        }
    }

    @Nested
    @DisplayName("Pruebas de Cálculo de Precision")
    class PrecisionCalculationTests {

        @Test
        @DisplayName("Debería calcular precision correctamente con valores válidos")
        void shouldCalculatePrecisionCorrectlyWithValidValues() {
            // Given
            evaluationMetrics.incrementTruePositives(); // 1
            evaluationMetrics.incrementTruePositives(); // 2
            evaluationMetrics.incrementFalsePositives(); // 1

            // When
            double precision = evaluationMetrics.getPrecision();

            // Then
            // Precision = TP / (TP + FP) = 2 / (2 + 1) = 2/3 ≈ 0.6667
            assertEquals(2.0 / 3.0, precision, 0.0001);
        }

        @Test
        @DisplayName("Debería retornar precision de 1.0 cuando no hay false positives")
        void shouldReturnPrecisionOneWhenNoFalsePositives() {
            // Given
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTruePositives();
            // No false positives

            // When
            double precision = evaluationMetrics.getPrecision();

            // Then
            // Precision = TP / (TP + FP) = 2 / (2 + 0) = 1.0
            assertEquals(1.0, precision, 0.0001);
        }

        @Test
        @DisplayName("Debería retornar precision de 0.0 cuando no hay true positives")
        void shouldReturnPrecisionZeroWhenNoTruePositives() {
            // Given
            evaluationMetrics.incrementFalsePositives();
            evaluationMetrics.incrementFalsePositives();
            // No true positives

            // When
            double precision = evaluationMetrics.getPrecision();

            // Then
            // Precision = TP / (TP + FP) = 0 / (0 + 2) = 0.0
            assertEquals(0.0, precision, 0.0001);
        }

        @Test
        @DisplayName("Debería manejar división por cero retornando 0.0")
        void shouldHandleDivisionByZeroReturningZero() {
            // Given - No true positives ni false positives

            // When
            double precision = evaluationMetrics.getPrecision();

            // Then
            assertEquals(0.0, precision, 0.0001);
        }
    }

    @Nested
    @DisplayName("Pruebas de Cálculo de Recall")
    class RecallCalculationTests {

        @Test
        @DisplayName("Debería calcular recall correctamente con valores válidos")
        void shouldCalculateRecallCorrectlyWithValidValues() {
            // Given
            evaluationMetrics.incrementTruePositives(); // 1
            evaluationMetrics.incrementTruePositives(); // 2
            evaluationMetrics.incrementFalseNegatives(); // 1

            // When
            double recall = evaluationMetrics.getRecall();

            // Then
            // Recall = TP / (TP + FN) = 2 / (2 + 1) = 2/3 ≈ 0.6667
            assertEquals(2.0 / 3.0, recall, 0.0001);
        }

        @Test
        @DisplayName("Debería retornar recall de 1.0 cuando no hay false negatives")
        void shouldReturnRecallOneWhenNoFalseNegatives() {
            // Given
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTruePositives();
            // No false negatives

            // When
            double recall = evaluationMetrics.getRecall();

            // Then
            // Recall = TP / (TP + FN) = 2 / (2 + 0) = 1.0
            assertEquals(1.0, recall, 0.0001);
        }

        @Test
        @DisplayName("Debería retornar recall de 0.0 cuando no hay true positives")
        void shouldReturnRecallZeroWhenNoTruePositives() {
            // Given
            evaluationMetrics.incrementFalseNegatives();
            evaluationMetrics.incrementFalseNegatives();
            // No true positives

            // When
            double recall = evaluationMetrics.getRecall();

            // Then
            // Recall = TP / (TP + FN) = 0 / (0 + 2) = 0.0
            assertEquals(0.0, recall, 0.0001);
        }

        @Test
        @DisplayName("Debería manejar división por cero en recall retornando 0.0")
        void shouldHandleDivisionByZeroInRecallReturningZero() {
            // Given - No true positives ni false negatives

            // When
            double recall = evaluationMetrics.getRecall();

            // Then
            assertEquals(0.0, recall, 0.0001);
        }
    }

    @Nested
    @DisplayName("Pruebas de Cálculo de F1-Score")
    class F1ScoreCalculationTests {

        @Test
        @DisplayName("Debería calcular f1-score correctamente con valores válidos")
        void shouldCalculateF1ScoreCorrectlyWithValidValues() {
            // Given
            evaluationMetrics.incrementTruePositives(); // 1
            evaluationMetrics.incrementTruePositives(); // 2
            evaluationMetrics.incrementFalsePositives(); // 1
            evaluationMetrics.incrementFalseNegatives(); // 1

            // When
            double f1Score = evaluationMetrics.getF1Score();

            // Then
            // Precision = 2 / (2 + 1) = 2/3
            // Recall = 2 / (2 + 1) = 2/3
            // F1 = 2 * (2/3 * 2/3) / (2/3 + 2/3) = 2 * (4/9) / (4/3) = (8/9) / (4/3) = 2/3
            assertEquals(2.0 / 3.0, f1Score, 0.0001);
        }

        @Test
        @DisplayName("Debería retornar f1-score de 1.0 cuando precision y recall son perfectos")
        void shouldReturnF1ScoreOneWhenPrecisionAndRecallArePerfect() {
            // Given
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTruePositives();
            // No false positives ni false negatives

            // When
            double f1Score = evaluationMetrics.getF1Score();

            // Then
            // Precision = 1.0, Recall = 1.0
            // F1 = 2 * (1.0 * 1.0) / (1.0 + 1.0) = 2 * 1.0 / 2.0 = 1.0
            assertEquals(1.0, f1Score, 0.0001);
        }

        @Test
        @DisplayName("Debería retornar f1-score de 0.0 cuando precision y recall son cero")
        void shouldReturnF1ScoreZeroWhenPrecisionAndRecallAreZero() {
            // Given - No true positives

            // When
            double f1Score = evaluationMetrics.getF1Score();

            // Then
            assertEquals(0.0, f1Score, 0.0001);
        }

        @Test
        @DisplayName("Debería manejar división por cero en f1-score retornando 0.0")
        void shouldHandleDivisionByZeroInF1ScoreReturningZero() {
            // Given - Precision y recall son ambos 0.0

            // When
            double f1Score = evaluationMetrics.getF1Score();

            // Then
            assertEquals(0.0, f1Score, 0.0001);
        }
    }

    @Nested
    @DisplayName("Pruebas de Cálculo de Accuracy")
    class AccuracyCalculationTests {

        @Test
        @DisplayName("Debería calcular accuracy correctamente con valores válidos")
        void shouldCalculateAccuracyCorrectlyWithValidValues() {
            // Given
            evaluationMetrics.incrementTruePositives(); // 1
            evaluationMetrics.incrementTruePositives(); // 2
            evaluationMetrics.incrementFalsePositives(); // 1
            evaluationMetrics.incrementFalseNegatives(); // 1
            evaluationMetrics.incrementTrueNegatives(); // 1
            evaluationMetrics.incrementTrueNegatives(); // 2

            // When
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            // Accuracy = (TP + TN) / (TP + TN + FP + FN) = (2 + 2) / (2 + 2 + 1 + 1) = 4/6 = 2/3
            assertEquals(2.0 / 3.0, accuracy, 0.0001);
        }

        @Test
        @DisplayName("Debería retornar accuracy de 1.0 cuando todas las predicciones son correctas")
        void shouldReturnAccuracyOneWhenAllPredictionsAreCorrect() {
            // Given
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTrueNegatives();
            evaluationMetrics.incrementTrueNegatives();
            // No false positives ni false negatives

            // When
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            // Accuracy = (2 + 2) / (2 + 2 + 0 + 0) = 4/4 = 1.0
            assertEquals(1.0, accuracy, 0.0001);
        }

        @Test
        @DisplayName("Debería retornar accuracy de 0.0 cuando todas las predicciones son incorrectas")
        void shouldReturnAccuracyZeroWhenAllPredictionsAreIncorrect() {
            // Given
            evaluationMetrics.incrementFalsePositives();
            evaluationMetrics.incrementFalsePositives();
            evaluationMetrics.incrementFalseNegatives();
            evaluationMetrics.incrementFalseNegatives();
            // No true positives ni true negatives

            // When
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            // Accuracy = (0 + 0) / (0 + 0 + 2 + 2) = 0/4 = 0.0
            assertEquals(0.0, accuracy, 0.0001);
        }

        @Test
        @DisplayName("Debería manejar división por cero en accuracy retornando 0.0")
        void shouldHandleDivisionByZeroInAccuracyReturningZero() {
            // Given - No datos

            // When
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            assertEquals(0.0, accuracy, 0.0001);
        }
    }

    @Nested
    @DisplayName("Pruebas de Escenarios Reales")
    class RealWorldScenariosTests {

        @Test
        @DisplayName("Debería manejar escenario de clasificador perfecto")
        void shouldHandlePerfectClassifierScenario() {
            // Given - Clasificador perfecto: solo true positives y true negatives
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTrueNegatives();
            evaluationMetrics.incrementTrueNegatives();

            // When
            double precision = evaluationMetrics.getPrecision();
            double recall = evaluationMetrics.getRecall();
            double f1Score = evaluationMetrics.getF1Score();
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            assertEquals(1.0, precision, 0.0001); // 3/3 = 1.0
            assertEquals(1.0, recall, 0.0001);    // 3/3 = 1.0
            assertEquals(1.0, f1Score, 0.0001);  // F1 perfecto
            assertEquals(1.0, accuracy, 0.0001);  // 5/5 = 1.0
        }

        @Test
        @DisplayName("Debería manejar escenario de clasificador aleatorio")
        void shouldHandleRandomClassifierScenario() {
            // Given - Clasificador aleatorio: distribución balanceada
            evaluationMetrics.incrementTruePositives();  // 1
            evaluationMetrics.incrementTruePositives();  // 2
            evaluationMetrics.incrementFalsePositives(); // 1
            evaluationMetrics.incrementFalseNegatives(); // 1
            evaluationMetrics.incrementTrueNegatives();  // 1
            evaluationMetrics.incrementTrueNegatives(); // 2

            // When
            double precision = evaluationMetrics.getPrecision();
            double recall = evaluationMetrics.getRecall();
            double f1Score = evaluationMetrics.getF1Score();
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            assertEquals(2.0 / 3.0, precision, 0.0001); // 2/(2+1) = 2/3
            assertEquals(2.0 / 3.0, recall, 0.0001);      // 2/(2+1) = 2/3
            assertEquals(2.0 / 3.0, f1Score, 0.0001);    // F1 = 2/3
            assertEquals(4.0 / 6.0, accuracy, 0.0001);    // (2+2)/(2+2+1+1) = 4/6
        }

        @Test
        @DisplayName("Debería manejar escenario de clasificador conservador")
        void shouldHandleConservativeClassifierScenario() {
            // Given - Clasificador conservador: pocos false positives, muchos false negatives
            evaluationMetrics.incrementTruePositives();  // 1
            evaluationMetrics.incrementFalseNegatives();  // 1
            evaluationMetrics.incrementFalseNegatives();  // 2
            evaluationMetrics.incrementFalseNegatives();  // 3
            evaluationMetrics.incrementTrueNegatives();   // 1
            evaluationMetrics.incrementTrueNegatives();   // 2
            evaluationMetrics.incrementTrueNegatives();   // 3
            evaluationMetrics.incrementTrueNegatives();   // 4

            // When
            double precision = evaluationMetrics.getPrecision();
            double recall = evaluationMetrics.getRecall();
            double f1Score = evaluationMetrics.getF1Score();
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            assertEquals(1.0, precision, 0.0001);        // 1/(1+0) = 1.0
            assertEquals(0.25, recall, 0.0001);          // 1/(1+3) = 0.25
            assertEquals(0.4, f1Score, 0.0001);          // 2*(1.0*0.25)/(1.0+0.25) = 0.4
            assertEquals(5.0 / 8.0, accuracy, 0.0001);    // (1+4)/(1+4+0+3) = 5/8
        }

        @Test
        @DisplayName("Debería manejar escenario de clasificador agresivo")
        void shouldHandleAggressiveClassifierScenario() {
            // Given - Clasificador agresivo: muchos false positives, pocos false negatives
            evaluationMetrics.incrementTruePositives();   // 1
            evaluationMetrics.incrementTruePositives();   // 2
            evaluationMetrics.incrementTruePositives();   // 3
            evaluationMetrics.incrementFalsePositives();  // 1
            evaluationMetrics.incrementFalsePositives();  // 2
            evaluationMetrics.incrementFalsePositives();  // 3
            evaluationMetrics.incrementFalsePositives();  // 4
            evaluationMetrics.incrementTrueNegatives();   // 1

            // When
            double precision = evaluationMetrics.getPrecision();
            double recall = evaluationMetrics.getRecall();
            double f1Score = evaluationMetrics.getF1Score();
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            assertEquals(3.0 / 7.0, precision, 0.0001);  // 3/(3+4) = 3/7
            assertEquals(1.0, recall, 0.0001);           // 3/(3+0) = 1.0
            assertEquals(6.0 / 10.0, f1Score, 0.0001);   // 2*(3/7*1.0)/(3/7+1.0) = 6/10
            assertEquals(4.0 / 8.0, accuracy, 0.0001);    // (3+1)/(3+1+4+0) = 4/8
        }
    }

    @Nested
    @DisplayName("Pruebas de Casos Edge")
    class EdgeCasesTests {

        @Test
        @DisplayName("Debería manejar valores muy grandes")
        void shouldHandleVeryLargeValues() {
            // Given - Valores grandes
            for (int i = 0; i < 1000; i++) {
                evaluationMetrics.incrementTruePositives();
                evaluationMetrics.incrementFalsePositives();
                evaluationMetrics.incrementFalseNegatives();
                evaluationMetrics.incrementTrueNegatives();
            }

            // When
            double precision = evaluationMetrics.getPrecision();
            double recall = evaluationMetrics.getRecall();
            double f1Score = evaluationMetrics.getF1Score();
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            assertEquals(0.5, precision, 0.0001); // 1000/(1000+1000) = 0.5
            assertEquals(0.5, recall, 0.0001);   // 1000/(1000+1000) = 0.5
            assertEquals(0.5, f1Score, 0.0001);   // F1 = 0.5
            assertEquals(0.5, accuracy, 0.0001); // (1000+1000)/(1000+1000+1000+1000) = 0.5
        }

        @Test
        @DisplayName("Debería manejar estado inicial sin datos")
        void shouldHandleInitialStateWithNoData() {
            // When - Sin incrementos
            double precision = evaluationMetrics.getPrecision();
            double recall = evaluationMetrics.getRecall();
            double f1Score = evaluationMetrics.getF1Score();
            double accuracy = evaluationMetrics.getAccuracy();

            // Then - Todos deberían ser 0.0
            assertEquals(0.0, precision, 0.0001);
            assertEquals(0.0, recall, 0.0001);
            assertEquals(0.0, f1Score, 0.0001);
            assertEquals(0.0, accuracy, 0.0001);
        }

        @Test
        @DisplayName("Debería manejar solo true positives")
        void shouldHandleOnlyTruePositives() {
            // Given
            evaluationMetrics.incrementTruePositives();
            evaluationMetrics.incrementTruePositives();

            // When
            double precision = evaluationMetrics.getPrecision();
            double recall = evaluationMetrics.getRecall();
            double f1Score = evaluationMetrics.getF1Score();
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            assertEquals(1.0, precision, 0.0001); // 2/(2+0) = 1.0
            assertEquals(1.0, recall, 0.0001);     // 2/(2+0) = 1.0
            assertEquals(1.0, f1Score, 0.0001);    // F1 = 1.0
            assertEquals(1.0, accuracy, 0.0001);   // (2+0)/(2+0+0+0) = 1.0
        }

        @Test
        @DisplayName("Debería manejar solo true negatives")
        void shouldHandleOnlyTrueNegatives() {
            // Given
            evaluationMetrics.incrementTrueNegatives();
            evaluationMetrics.incrementTrueNegatives();

            // When
            double precision = evaluationMetrics.getPrecision();
            double recall = evaluationMetrics.getRecall();
            double f1Score = evaluationMetrics.getF1Score();
            double accuracy = evaluationMetrics.getAccuracy();

            // Then
            assertEquals(0.0, precision, 0.0001); // 0/(0+0) = 0.0
            assertEquals(0.0, recall, 0.0001);      // 0/(0+0) = 0.0
            assertEquals(0.0, f1Score, 0.0001);     // F1 = 0.0
            assertEquals(1.0, accuracy, 0.0001);   // (0+2)/(0+2+0+0) = 1.0
        }
    }
}
