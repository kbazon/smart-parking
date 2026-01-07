package com.smartparking.smart_parking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.smart_parking.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class ParkingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ParkingEventPublisher.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper om = new ObjectMapper();

    public ParkingEventPublisher(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void publishEvent(String type, Ticket ticket) {
        // stari stub log (ostaje)
        log.info("Event published: {} | Ticket UUID: {}", type, ticket.getTicketUuid());

        // novi dio: publish u Redis kanal
        try {
            String payload = om.writeValueAsString(Map.of(
                    "type", type,
                    "ticketUuid", ticket.getTicketUuid().toString(),
                    "timestamp", Instant.now().toString()
            ));
            redis.convertAndSend("parking-events", payload);
            log.info("Published to Redis channel parking-events: {}", payload);
        } catch (Exception e) {
            log.error("Failed to publish event to Redis", e);
        }
    }
}
