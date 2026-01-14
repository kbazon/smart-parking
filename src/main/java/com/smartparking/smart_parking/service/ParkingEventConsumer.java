package com.smartparking.smart_parking.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(name = "parking.consumer.enabled", havingValue = "true")
@Service
public class ParkingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ParkingEventConsumer.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper om = new ObjectMapper();

    public ParkingEventConsumer(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void handleMessage(String message) {
        try {
            JsonNode root = om.readTree(message);
            String type = root.get("type").asText();

            String freeKey = "availability:total:free";
            String capKey = "capacity:total";

            int capacity = Integer.parseInt(redis.opsForValue().get(capKey));
            Long newFree;

            if ("ENTRY".equals(type)) {
                newFree = redis.opsForValue().increment(freeKey, -1);
            } else if ("EXIT".equals(type)) {
                newFree = redis.opsForValue().increment(freeKey, 1);
            } else {
                return;
            }

            if (newFree == null) return;

            // clamp
            if (newFree < 0) redis.opsForValue().set(freeKey, "0");
            if (newFree > capacity) redis.opsForValue().set(freeKey, String.valueOf(capacity));

            log.info("Handled {} -> free={}", type, redis.opsForValue().get(freeKey));

        } catch (Exception e) {
            log.error("Failed to handle event message: {}", message, e);
        }
    }
}
