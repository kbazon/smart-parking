package com.smartparking.smart_parking.controller;

import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AvailabilityController {

    private final StringRedisTemplate redis;

    public AvailabilityController(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @GetMapping("/availability/total")
    public Map<String, Integer> totalAvailability() {
        int capacity = Integer.parseInt(redis.opsForValue().get("capacity:total"));
        int free = Integer.parseInt(redis.opsForValue().get("availability:total:free"));
        int occupied = Math.max(0, capacity - free);

        return Map.of(
                "capacity", capacity,
                "free", free,
                "occupied", occupied
        );
    }
}