package com.smartparking.smart_parking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityInitializer implements CommandLineRunner {

    private static final String DEFAULT_CAPACITY = "200";

    private final StringRedisTemplate redis;

    public AvailabilityInitializer(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void run(String... args) {
        String capacityKey = "capacity:total";
        String freeKey = "availability:total:free";

        redis.opsForValue().setIfAbsent(capacityKey, DEFAULT_CAPACITY);

        if (redis.opsForValue().get(freeKey) == null) {
            redis.opsForValue().set(freeKey, redis.opsForValue().get(capacityKey));
        }

        System.out.println("INIT capacity=" + redis.opsForValue().get(capacityKey)
                + ", free=" + redis.opsForValue().get(freeKey));
    }
}