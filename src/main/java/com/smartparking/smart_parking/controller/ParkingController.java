package com.smartparking.smart_parking.controller;

import com.smartparking.smart_parking.dto.OccupancyResponse;
import com.smartparking.smart_parking.dto.PredictionRequest;
import com.smartparking.smart_parking.service.DataIngestionService;
import com.smartparking.smart_parking.service.FeaturePreprocessorService;
import com.smartparking.smart_parking.service.PredictionClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes parking prediction endpoints.
 *
 * New endpoints added:
 *
 *   GET  /api/parking/predict/{lotId}
 *        Returns the latest cached prediction for the lot.
 *        If Redis has no data yet, triggers a live call to ml-service.
 *
 *   POST /api/parking/predict/{lotId}/refresh
 *        Forces re-computation: queries DB → builds features → calls ml-service.
 *        Useful for demos and the live-data coursework requirement.
 *
 *
 * Your existing endpoints (status, lots, etc.) are NOT touched.
 */
@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final DataIngestionService       ingestion;
    private final FeaturePreprocessorService preprocessor;
    private final PredictionClient           predictionClient;

    public ParkingController(
            DataIngestionService ingestion,
            FeaturePreprocessorService preprocessor,
            PredictionClient predictionClient) {
        this.ingestion        = ingestion;
        this.preprocessor     = preprocessor;
        this.predictionClient = predictionClient;
    }

    // ── GET /api/parking/predict/{lotId} ─────────────────────────────────────
    //Returns the latest next-hour high-occupancy forecast for a lot.
    // PredictionClient.predict() serves from Redis if available;
    // falls back to a live ML call if the cache is cold.
    @GetMapping("/predict/{lotId}")
    public ResponseEntity<OccupancyResponse> getPrediction(
            @PathVariable String lotId,
            @RequestParam(required = false, defaultValue = "false") boolean refresh) {

        OccupancyResponse response = refresh
                ? ingestion.runNow(lotId)                              // force live call
                : predictionClient.predict(lotId,                      // cache-first
                        preprocessor.buildForLot(lotId, false));

        return ResponseEntity.ok(response);
    }
    
 // Returns the current feature vector used to predict next-hour high occupancy.

    @GetMapping("/features/{lotId}")
    public ResponseEntity<PredictionRequest> getFeatures(@PathVariable String lotId) {
        PredictionRequest features = preprocessor.buildForLot(lotId, false);
        return ResponseEntity.ok(features);
    }
    
    

    // ── POST /api/parking/predict/{lotId}/refresh ─────────────────────────────
    // Explicit refresh — triggers the full pipeline for one lot.
    @PostMapping("/predict/{lotId}/refresh")
    public ResponseEntity<OccupancyResponse> refreshPrediction(
            @PathVariable String lotId) {
        OccupancyResponse response = ingestion.runNow(lotId);
        return ResponseEntity.ok(response);
    }
}
