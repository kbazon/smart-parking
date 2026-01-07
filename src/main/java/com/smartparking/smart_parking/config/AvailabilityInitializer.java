package  com.smartparking.smart_parking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityInitializer implements CommandLineRunner {

    private final StringRedisTemplate redis;

    public AvailabilityInitializer(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void run(String... args) {
        String capacityKey = "capacity:total";
        String freeKey = "availability:total:free";

        // ako nema capacity -> postavi
        redis.opsForValue().setIfAbsent(capacityKey, "100");

        // ako nema free -> free = capacity
        redis.opsForValue().setIfAbsent(freeKey, redis.opsForValue().get(capacityKey));

        System.out.println("INIT capacity=" + redis.opsForValue().get(capacityKey)
                + ", free=" + redis.opsForValue().get(freeKey));
    }
}