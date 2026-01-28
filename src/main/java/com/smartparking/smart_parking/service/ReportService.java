package com.smartparking.smart_parking.service;

import com.smartparking.smart_parking.model.Report;
import com.smartparking.smart_parking.repository.ReportRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class ReportService {

    private final EntityManager em;
    private final ReportRepository reportRepository;
    private final StringRedisTemplate redis;

    public ReportService(EntityManager em, ReportRepository reportRepository, StringRedisTemplate redis) {
        this.em = em;
        this.reportRepository = reportRepository;
        this.redis = redis;
    }

    @Transactional
    public Report generateDailyReport(LocalDate date) {
        return generateReport(date, date, "DAILY");
    }

    @Transactional
    public Report generateMonthlyReport(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return generateReport(start, end, "MONTHLY");
    }

    private Report generateReport(LocalDate startDate, LocalDate endDate, String type) {
        // Peak hour
        Query qPeak = em.createNativeQuery(
            "SELECT EXTRACT(HOUR FROM entry_time) AS hr " +
            "FROM tickets " +
            "WHERE entry_time::date BETWEEN :start AND :end " +
            "GROUP BY hr " +
            "ORDER BY COUNT(*) DESC " +
            "LIMIT 1"
        );
        qPeak.setParameter("start", startDate);
        qPeak.setParameter("end", endDate);
        Integer peakHour = null;
        try {
            peakHour = ((Number) qPeak.getSingleResult()).intValue();
        } catch (Exception e) {
            peakHour = 0;
        }

        // prosjecno trajanje
        Query qAvgDuration = em.createNativeQuery(
            "SELECT AVG(EXTRACT(EPOCH FROM (exit_time - entry_time))/3600) " +
            "FROM tickets WHERE exit_time IS NOT NULL AND entry_time::date BETWEEN :start AND :end"
        );
        qAvgDuration.setParameter("start", startDate);
        qAvgDuration.setParameter("end", endDate);
        Double avgDuration = ((Number) qAvgDuration.getSingleResult() != null) ? 
                             ((Number) qAvgDuration.getSingleResult()).doubleValue() : 0.0;

        //prihod
        Query qRevenue = em.createNativeQuery(
            "SELECT COALESCE(SUM(price),0) FROM tickets WHERE entry_time::date BETWEEN :start AND :end"
        );
        qRevenue.setParameter("start", startDate);
        qRevenue.setParameter("end", endDate);
        Double revenue = ((Number) qRevenue.getSingleResult()).doubleValue();

        // stopa popunjenosti (Redis)
        String capacityStr = redis.opsForValue().get("capacity:total");
        int capacity = capacityStr != null ? Integer.parseInt(capacityStr) : 100;

        String freeStr = redis.opsForValue().get("availability:total:free");
        int free = freeStr != null ? Integer.parseInt(freeStr) : capacity;

        double occupancyRate = capacity > 0 ? ((double)(capacity - free) / capacity) * 100 : 0.0;

        Report report = new Report();
        report.setReportDate(startDate); 
        report.setType(type);
        report.setPeakHour(peakHour);
        report.setAvgDuration(avgDuration);
        report.setRevenue(revenue);
        report.setOccupancyRate(occupancyRate);
        report.setCurrency("EUR");


        return reportRepository.save(report);
    }
}
