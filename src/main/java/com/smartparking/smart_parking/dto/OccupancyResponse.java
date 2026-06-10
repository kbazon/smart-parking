package com.smartparking.smart_parking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

public class OccupancyResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("prediction")
    private int prediction;

    @JsonProperty("probability")
    private double probability;

    @JsonProperty("model_used")
    private String modelUsed;
    
    @JsonProperty("prediction_horizon_minutes")
    private int predictionHorizonMinutes = 60;

    @JsonProperty("target_threshold")
    private double targetThreshold = 0.85;

    @JsonProperty("target_description")
    private String targetDescription = "Probability that LOT_A occupancy will be at least 85% in the next hour";

    public OccupancyResponse() {}

    public OccupancyResponse(int prediction, double probability, String modelUsed) {
        this.prediction  = prediction;
        this.probability = probability;
        this.modelUsed   = modelUsed;
    }
    
    @JsonIgnore
    public boolean isHighOccupancy() {
        return prediction == 1;
    }

    @JsonProperty("high_occupancy_next_hour")
    public boolean isHighOccupancyNextHour() {
        return prediction == 1;
    }

    public String probabilityPercent() {
        return String.format("%.1f%%", probability * 100);
    }

    public int    getPrediction()           { return prediction; }
    public void   setPrediction(int v)      { this.prediction = v; }

    public double getProbability()          { return probability; }
    public void   setProbability(double v)  { this.probability = v; }

    public String getModelUsed()            { return modelUsed; }
    public void   setModelUsed(String v)    { this.modelUsed = v; }
    
    public int getPredictionHorizonMinutes() {
        return predictionHorizonMinutes;
    }

    public void setPredictionHorizonMinutes(int predictionHorizonMinutes) {
        this.predictionHorizonMinutes = predictionHorizonMinutes;
    }

    public double getTargetThreshold() {
        return targetThreshold;
    }

    public void setTargetThreshold(double targetThreshold) {
        this.targetThreshold = targetThreshold;
    }

    public String getTargetDescription() {
        return targetDescription;
    }

    public void setTargetDescription(String targetDescription) {
        this.targetDescription = targetDescription;
    }

    @Override
    public String toString() {
        return "OccupancyResponse{prediction=" + prediction
                + ", probability=" + probabilityPercent()
                + ", model='" + modelUsed + "'}";
    }
}