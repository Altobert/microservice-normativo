package cl.sii.normativo.loadnormas.pruebascobertura;

import org.springframework.stereotype.Component;
@Component
public class EvaluationMetrics {

    private int truePositives;
    private int falsePositives;
    private int falseNegatives;
    private int trueNegatives;

    public void incrementTruePositives() {
        this.truePositives++;
    }

    public void incrementFalsePositives() {
        this.falsePositives++;
    }

    public void incrementFalseNegatives() {
        this.falseNegatives++;
    }

    public void incrementTrueNegatives() {
        this.trueNegatives++;
    }

    public int getTruePositives() {
        return this.truePositives;
    }

    public int getFalsePositives() {
        return this.falsePositives;
    }

    public int getFalseNegatives() {
        return this.falseNegatives;
    }

    public int getTrueNegatives() {
        return this.trueNegatives;
    }

    public double getPrecision() {
        int denominator = truePositives + falsePositives;
        if (denominator == 0) {
            return 0.0;
        }
        return (double) truePositives / denominator;
    }

    public double getRecall() {
        int denominator = truePositives + falseNegatives;
        if (denominator == 0) {
            return 0.0;
        }
        return (double) truePositives / denominator;
    }
    
    public double getF1Score() {
        double precision = getPrecision();
        double recall = getRecall();
        double denominator = precision + recall;
        if (denominator == 0) {
            return 0.0;
        }
        return 2 * (precision * recall) / denominator;
    }

    public double getAccuracy() {
        int total = truePositives + trueNegatives + falsePositives + falseNegatives;
        if (total == 0) {
            return 0.0;
        }
        return (double) (truePositives + trueNegatives) / total;
    }
}
