package com.smartparking.smart_parking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Sent as JSON body to POST http://ml-service:8000/predict
 *
 * Field names use @JsonProperty to produce snake_case JSON that matches
 * the Python Pydantic PredictRequest model exactly.
 * Java field names stay camelCase (convention); wire format is snake_case.
 */
public class PredictionRequest {

    // ── Numeric features ──────────────────────────────────────────────────────
    @JsonProperty("hour")
    private int hour;

    @JsonProperty("is_holiday")
    private int isHoliday;

    @JsonProperty("capacity")
    private int capacity;

    @JsonProperty("occupied_count")
    private int occupiedCount;

    @JsonProperty("occupancy_rate_lot")
    private double occupancyRateLot;

    @JsonProperty("occupied_count_lag_1h")
    private int occupiedCountLag1h;

    @JsonProperty("occupied_count_lag_2h")
    private int occupiedCountLag2h;

    @JsonProperty("occupied_count_lag_3h")
    private int occupiedCountLag3h;

    @JsonProperty("occupancy_rate_lag_1h")
    private double occupancyRateLag1h;

    @JsonProperty("occupancy_rate_lag_2h")
    private double occupancyRateLag2h;

    @JsonProperty("occupancy_rate_lag_3h")
    private double occupancyRateLag3h;

    // ── Categorical feature ───────────────────────────────────────────────────
    // "mon", "tue", "wed", "thu", "fri", "sat", "sun"
    @JsonProperty("day_of_week")
    private String dayOfWeek;

    // ── No-arg constructor (required by Jackson) ──────────────────────────────
    public PredictionRequest() {}

    // ── All-arg constructor (used by FeaturePreprocessorService) ─────────────
    public PredictionRequest(
            int hour, int isHoliday, int capacity, int occupiedCount,
            double occupancyRateLot,
            int occupiedCountLag1h, int occupiedCountLag2h, int occupiedCountLag3h,
            double occupancyRateLag1h, double occupancyRateLag2h, double occupancyRateLag3h,
            String dayOfWeek) {
        this.hour               = hour;
        this.isHoliday          = isHoliday;
        this.capacity           = capacity;
        this.occupiedCount      = occupiedCount;
        this.occupancyRateLot   = occupancyRateLot;
        this.occupiedCountLag1h = occupiedCountLag1h;
        this.occupiedCountLag2h = occupiedCountLag2h;
        this.occupiedCountLag3h = occupiedCountLag3h;
        this.occupancyRateLag1h = occupancyRateLag1h;
        this.occupancyRateLag2h = occupancyRateLag2h;
        this.occupancyRateLag3h = occupancyRateLag3h;
        this.dayOfWeek          = dayOfWeek;
    }

    // ── Getters and setters ───────────────────────────────────────────────────
    public int    getHour()               { return hour; }
    public void   setHour(int v)          { this.hour = v; }

    public int    getIsHoliday()          { return isHoliday; }
    public void   setIsHoliday(int v)     { this.isHoliday = v; }

    public int    getCapacity()           { return capacity; }
    public void   setCapacity(int v)      { this.capacity = v; }

    public int    getOccupiedCount()      { return occupiedCount; }
    public void   setOccupiedCount(int v) { this.occupiedCount = v; }

    public double getOccupancyRateLot()            { return occupancyRateLot; }
    public void   setOccupancyRateLot(double v)    { this.occupancyRateLot = v; }

    public int    getOccupiedCountLag1h()          { return occupiedCountLag1h; }
    public void   setOccupiedCountLag1h(int v)     { this.occupiedCountLag1h = v; }

    public int    getOccupiedCountLag2h()          { return occupiedCountLag2h; }
    public void   setOccupiedCountLag2h(int v)     { this.occupiedCountLag2h = v; }

    public int    getOccupiedCountLag3h()          { return occupiedCountLag3h; }
    public void   setOccupiedCountLag3h(int v)     { this.occupiedCountLag3h = v; }

    public double getOccupancyRateLag1h()          { return occupancyRateLag1h; }
    public void   setOccupancyRateLag1h(double v)  { this.occupancyRateLag1h = v; }

    public double getOccupancyRateLag2h()          { return occupancyRateLag2h; }
    public void   setOccupancyRateLag2h(double v)  { this.occupancyRateLag2h = v; }

    public double getOccupancyRateLag3h()          { return occupancyRateLag3h; }
    public void   setOccupancyRateLag3h(double v)  { this.occupancyRateLag3h = v; }

    public String getDayOfWeek()           { return dayOfWeek; }
    public void   setDayOfWeek(String v)   { this.dayOfWeek = v; }
}
