package com.smartparking.smart_parking.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reports", schema = "public")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "type", nullable = false)
    private String type; // DAILY / MONTHLY

    @Column(name = "peak_hour")
    private Integer peakHour;

    @Column(name = "occupancy_rate")
    private Double occupancyRate;

    @Column(name = "avg_duration")
    private Double avgDuration;

    @Column(name = "revenue")
    private Double revenue;
    
    @Column(name = "currency")
    private String currency = "EUR";

    // Getteri i setteri
    public Long getReportId() { return reportId; }
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getPeakHour() { return peakHour; }
    public void setPeakHour(Integer peakHour) { this.peakHour = peakHour; }
    public Double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(Double occupancyRate) { this.occupancyRate = occupancyRate; }
    public Double getAvgDuration() { return avgDuration; }
    public void setAvgDuration(Double avgDuration) { this.avgDuration = avgDuration; }
    public Double getRevenue() { return revenue; }
    public void setRevenue(Double revenue) { this.revenue = revenue; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
