package com.smartparking.smart_parking.repository;

import com.smartparking.smart_parking.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    // Kasnije se moze dodati ostale metode
}
