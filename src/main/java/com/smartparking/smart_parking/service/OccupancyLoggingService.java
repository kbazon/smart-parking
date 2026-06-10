package com.smartparking.smart_parking.service;

import com.smartparking.smart_parking.model.ParkingOccupancyLog;
import com.smartparking.smart_parking.repository.ParkingOccupancyLogRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OccupancyLoggingService {

    private static final String LOT_ID = "LOT_A";
    private static final int DEFAULT_CAPACITY = 200;

    private static final String CAPACITY_KEY = "capacity:total";
    private static final String FREE_KEY = "availability:total:free";

    private static final List<String> HOLIDAY_MONTH_DAYS = List.of(
            "01-01", "01-06", "05-01", "05-30", "06-22", "08-05",
            "08-15", "11-01", "11-18", "12-25", "12-26"
    );

    private final ParkingOccupancyLogRepository repository;
    private final StringRedisTemplate redis;

    public OccupancyLoggingService(
            ParkingOccupancyLogRepository repository,
            StringRedisTemplate redis
    ) {
        this.repository = repository;
        this.redis = redis;
    }

    @Transactional
    public void logCurrentOccupancy() {
    	int capacity = parseOrDefault(redis.opsForValue().get(CAPACITY_KEY), DEFAULT_CAPACITY);

    	if (capacity <= 0) {
    	    capacity = DEFAULT_CAPACITY;
    	}

        int free = parseOrDefault(redis.opsForValue().get(FREE_KEY), capacity);

        if (free < 0) {
            free = 0;
        }

        if (free > capacity) {
            free = capacity;
        }

        int occupiedCount = capacity - free;

        ParkingOccupancyLog snapshot = new ParkingOccupancyLog();
        snapshot.setLotId(LOT_ID);
        snapshot.setTs(LocalDateTime.now());
        snapshot.setOccupiedCount(occupiedCount);
        snapshot.setCapacity(capacity);
        snapshot.setIsHoliday(isTodayHoliday() ? (short) 1 : (short) 0);

        repository.save(snapshot);
    }

    private int parseOrDefault(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private boolean isTodayHoliday() {
        LocalDate today = LocalDate.now();
        String monthDay = String.format("%02d-%02d", today.getMonthValue(), today.getDayOfMonth());
        return HOLIDAY_MONTH_DAYS.contains(monthDay);
    }
}