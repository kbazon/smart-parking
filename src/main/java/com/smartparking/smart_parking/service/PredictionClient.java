package com.smartparking.smart_parking.service;

import com.smartparking.smart_parking.dto.OccupancyResponse;
import com.smartparking.smart_parking.dto.PredictionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import java.util.concurrent.TimeUnit;

@Service
public class PredictionClient {

    private static final Logger log = LoggerFactory.getLogger(PredictionClient.class);
    private static final String REDIS_KEY_PREFIX = "prediction:";
    private static final long REDIS_TTL_HOURS = 2;

    private final RestClient mlRestClient;
    private final RedisTemplate<String, Object> redis;
    private final ObjectMapper objectMapper;

    public PredictionClient(
            @Qualifier("mlRestClient") RestClient mlRestClient,
            RedisTemplate<String, Object> redis,
            ObjectMapper objectMapper) {
        this.mlRestClient = mlRestClient;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public OccupancyResponse predict(String lotId, PredictionRequest request) {
        try {
            log.info("REQUEST TO ML [{}]: {}", lotId, objectMapper.writeValueAsString(request));

            OccupancyResponse response = mlRestClient.post()
                    .uri("/predict")
                    .body(request)                        // Jackson serializira direktno
                    .retrieve()
                    .body(OccupancyResponse.class);

            if (response != null) {
                cacheResult(lotId, response);
                log.info("ML RESPONSE [{}]: next-hour pred={}, prob={}",
                        lotId, response.getPrediction(), response.getProbability());
                return response;
            }

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("ML HTTP error [{}]: {} - {}", lotId, ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("ML service error [{}]: {}", ex.getMessage());
        }
        return fallback(lotId);
    }

    

    // cacheResult i fallback ostaju isti kao prije


    private void cacheResult(String lotId, OccupancyResponse response) {
        String key = REDIS_KEY_PREFIX + lotId;

        try {
            redis.opsForValue().set(key, response, REDIS_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception ex) {
            log.warn("Redis cache write failed for {}: {}", lotId, ex.getMessage());
        }
    }

    private OccupancyResponse fallback(String lotId) {
        String key = REDIS_KEY_PREFIX + lotId;

        try {
            Object cached = redis.opsForValue().get(key);

            if (cached != null) {
                OccupancyResponse response = objectMapper.convertValue(
                        cached,
                        OccupancyResponse.class
                );

                log.warn("Using cached prediction for {}: {}", lotId, response);
                return response;
            }

        } catch (Exception ex) {
            log.warn("Redis cache read failed for {}: {}", lotId, ex.getMessage());
        }

        log.warn("No cache for {} — returning default fallback", lotId);
        return new OccupancyResponse(0, 0.0, "fallback");
    }
}