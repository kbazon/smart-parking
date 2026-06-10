package com.smartparking.smart_parking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_occupancy_log", schema = "public")
public class ParkingOccupancyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lot_id", nullable = false, length = 50)
    private String lotId;

    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    @Column(name = "occupied_count", nullable = false)
    private Integer occupiedCount;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "is_holiday", nullable = false)
    private Short isHoliday = 0;

    public ParkingOccupancyLog() {
    }

    public Long getId() {
        return id;
    }

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public LocalDateTime getTs() {
        return ts;
    }

    public void setTs(LocalDateTime ts) {
        this.ts = ts;
    }

    public Integer getOccupiedCount() {
        return occupiedCount;
    }

    public void setOccupiedCount(Integer occupiedCount) {
        this.occupiedCount = occupiedCount;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Short getIsHoliday() {
        return isHoliday;
    }

    public void setIsHoliday(Short isHoliday) {
        this.isHoliday = isHoliday;
    }
}