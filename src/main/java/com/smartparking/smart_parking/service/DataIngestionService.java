package com.smartparking.smart_parking.service;

import com.smartparking.smart_parking.dto.OccupancyResponse;
import com.smartparking.smart_parking.dto.PredictionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled data pipeline:
 *
 *   PostgreSQL (parking_occupancy_log)
 *       └─► FeaturePreprocessorService   (compute lag features)
 *               └─► PredictionClient     (POST /predict to ml-service)
 *                       └─► Redis        (store result for API reads)
 *
 * Runs every 15 minutes for all known parking lots.
 * Also exposes runNow() for on-demand refresh triggered by ParkingController.
 * The output is a next-hour high-occupancy forecast, not the current occupancy value.
 */
@Service
@EnableScheduling
public class DataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionService.class);

    // Redis key for storing all lot predictions as a JSON map
    private static final String REDIS_ALL_PREDICTIONS = "predictions:all";

    private final FeaturePreprocessorService preprocessor;
    private final PredictionClient           predictionClient;
    private final JdbcTemplate               jdbc;
    private final RedisTemplate<String, Object> redis;

    // Hardcoded holiday check — replace with a DB table or external API if needed
    // Format: MM-DD
    private static final List<String> HOLIDAY_MONTH_DAYS = List.of(
            "01-01", "01-06", "05-01", "05-30", "06-22", "08-05",
            "08-15", "11-01", "11-18", "12-25", "12-26"
    );

    public DataIngestionService(
            FeaturePreprocessorService preprocessor,
            PredictionClient predictionClient,
            JdbcTemplate jdbc,
            RedisTemplate<String, Object> redis) {
        this.preprocessor     = preprocessor;
        this.predictionClient = predictionClient;
        this.jdbc             = jdbc;
        this.redis            = redis;
    }

    /**
     * Runs automatically every 15 minutes.
     * cron = "0 0/15 * * * *"  → seconds minutes hours ...
     */
    @Scheduled(cron = "0 0/15 * * * *")
    public void runScheduled() {
        log.info("DataIngestionService: scheduled pipeline started");
        runForAllLots();
    }

    /**
     * Called by ParkingController for on-demand refresh.
     *The output is a next-hour high-occupancy forecast, not the current occupancy value.
     */
    public OccupancyResponse runNow(String lotId) {
        boolean holiday = isTodayHoliday();
        PredictionRequest req = preprocessor.buildForLot(lotId, holiday);
        return predictionClient.predict(lotId, req);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void runForAllLots() {
        List<String> lots = fetchAllLotIds();
        boolean holiday   = isTodayHoliday();

        for (String lotId : lots) {
            try {
                PredictionRequest req  = preprocessor.buildForLot(lotId, holiday);
                OccupancyResponse resp = predictionClient.predict(lotId, req);
                log.info("  {} → next-hour high-occupancy pred={} prob={}",
                        lotId, resp.getPrediction(), resp.probabilityPercent());
            } catch (Exception ex) {
                log.error("  Pipeline failed for lot {}: {}", lotId, ex.getMessage());
            }
        }
    }

    private List<String> fetchAllLotIds() {
        String sql = "SELECT DISTINCT lot_id FROM parking_occupancy_log";
        try {
            return jdbc.queryForList(sql, String.class);
        } catch (Exception ex) {
            log.warn("Could not fetch lot IDs: {}", ex.getMessage());
            return List.of();
        }
    }

    private boolean isTodayHoliday() {
        LocalDate today   = LocalDate.now();
        String    monthDay = String.format("%02d-%02d", today.getMonthValue(), today.getDayOfMonth());
        return HOLIDAY_MONTH_DAYS.contains(monthDay);
    }
}
