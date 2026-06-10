package com.smartparking.smart_parking.service;

import com.smartparking.smart_parking.dto.PredictionRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;


@Service
public class FeaturePreprocessorService {
	private static final int DEFAULT_CAPACITY = 200;

    private final JdbcTemplate jdbc;

    public FeaturePreprocessorService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public PredictionRequest buildForLot(String lotId, boolean isHoliday) {

        LocalDateTime now = LocalDateTime.now();

        LotSnapshot current = fetchClosestSnapshot(lotId, now, 0);
        LotSnapshot lag1 = fetchClosestSnapshot(lotId, now.minusHours(1), 1);
        LotSnapshot lag2 = fetchClosestSnapshot(lotId, now.minusHours(2), 2);
        LotSnapshot lag3 = fetchClosestSnapshot(lotId, now.minusHours(3), 3);

        String dayOfWeek = mapDayOfWeek(now.getDayOfWeek());

        return new PredictionRequest(
                now.getHour(),
                isHoliday ? 1 : 0,
                current.capacity,
                current.occupiedCount,
                current.occupancyRate(),
                lag1.occupiedCount,
                lag2.occupiedCount,
                lag3.occupiedCount,
                lag1.occupancyRate(),
                lag2.occupancyRate(),
                lag3.occupancyRate(),
                dayOfWeek
        );
    }
    

    // ─────────────────────────────────────────────────────────────
    // DAY OF WEEK MAPPING (MATCHES ML TRAINING DATASET)
    // ─────────────────────────────────────────────────────────────
    private String mapDayOfWeek(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "mon";
            case TUESDAY -> "tue";
            case WEDNESDAY -> "wed";
            case THURSDAY -> "thu";
            case FRIDAY -> "fri";
            case SATURDAY -> "sat";
            case SUNDAY -> "sun";
        };
    }

    // ─────────────────────────────────────────────────────────────
    // DATABASE HELPERS
    // ─────────────────────────────────────────────────────────────

    private LotSnapshot fetchClosestSnapshot(String lotId,
                                              LocalDateTime targetTime,
                                              int lagHours) {

        String sql = """
                SELECT occupied_count, capacity
                FROM parking_occupancy_log
                WHERE lot_id = ?
                  AND ts BETWEEN ? AND ?
                ORDER BY ABS(EXTRACT(EPOCH FROM (ts - ?::timestamp)))
                LIMIT 1
                """;

        LocalDateTime from = targetTime.minusMinutes(15);
        LocalDateTime to   = targetTime.plusMinutes(15);

        try {
            return jdbc.queryForObject(sql, (rs, _) -> new LotSnapshot(
                    rs.getInt("occupied_count"),
                    rs.getInt("capacity")
            ), lotId, from, to, targetTime);

        } catch (Exception ex) {
            int defaultCapacity = fetchCapacity(lotId);
            return new LotSnapshot(0, defaultCapacity);
        }
    }

    private int fetchCapacity(String lotId) {
        String sql = """
                SELECT capacity FROM parking_occupancy_log
                WHERE lot_id = ?
                ORDER BY ts DESC LIMIT 1
                """;

        try {
            Integer cap = jdbc.queryForObject(sql, Integer.class, lotId);
            return cap != null ? cap : DEFAULT_CAPACITY;
        } catch (Exception ex) {
        	return DEFAULT_CAPACITY;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // INNER MODEL
    // ─────────────────────────────────────────────────────────────
    private static class LotSnapshot {
        final int occupiedCount;
        final int capacity;

        LotSnapshot(int occupiedCount, int capacity) {
            this.occupiedCount = occupiedCount;
            this.capacity = capacity;
        }

        double occupancyRate() {
            if (capacity == 0) return 0.0;
            return Math.min(1.0, (double) occupiedCount / capacity);
        }
    }
}