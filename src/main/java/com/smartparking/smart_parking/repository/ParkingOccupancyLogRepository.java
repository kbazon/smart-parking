package com.smartparking.smart_parking.repository;

import com.smartparking.smart_parking.model.ParkingOccupancyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingOccupancyLogRepository extends JpaRepository<ParkingOccupancyLog, Long> {
}