package net.zcscloud.zhuohcun.zcorn.entity;

public class PredictionResponse {
    private String used_model;
    private String prediction;
    private double confidence;
    private java.util.Map<String, Double> probabilities;
    private double elapsed_ms;

    public String getUsed_model() { return used_model; }
    public void setUsed_model(String used_model) { this.used_model = used_model; }

    public String getPrediction() { return prediction; }
    public void setPrediction(String prediction) { this.prediction = prediction; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public java.util.Map<String, Double> getProbabilities() { return probabilities; }
    public void setProbabilities(java.util.Map<String, Double> probabilities) { this.probabilities = probabilities; }

    public double getElapsed_ms() { return elapsed_ms; }
    public void setElapsed_ms(double elapsed_ms) { this.elapsed_ms = elapsed_ms; }
}
