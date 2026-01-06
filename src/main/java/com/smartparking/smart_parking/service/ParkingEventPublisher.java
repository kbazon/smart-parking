package com.smartparking.smart_parking.service;

import com.smartparking.smart_parking.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ParkingEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(ParkingEventPublisher.class);

    public void publishEvent(String type, Ticket ticket) {
        log.info("Event stub published: {} | Ticket UUID: {}", type, ticket.getTicketUuid());
        //kasnije message broker
    }
}
