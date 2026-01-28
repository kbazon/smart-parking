package com.smartparking.smart_parking.controller;

import com.smartparking.smart_parking.model.Ticket;
import com.smartparking.smart_parking.repository.TicketRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.smartparking.smart_parking.service.ParkingEventPublisher;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    private final TicketRepository ticketRepository;
    private final ParkingEventPublisher eventPublisher; 
    private static final double PRICE_PER_HOUR_EUR = 2.0;

    public TicketController(TicketRepository ticketRepository, ParkingEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    //generiranje karte za ulaz
    @PostMapping("/entry")
    public Ticket createTicket(HttpServletRequest request) {
        Ticket ticket = new Ticket(); // UUID i entryTime auto
        Ticket saved = ticketRepository.save(ticket);

        log.info("Nova karta generirana: {} | Handled by port: {}", saved.getTicketUuid(), request.getLocalPort());
        eventPublisher.publishEvent("ENTRY", saved);
        return saved;
    }
    
    //izlaz i cijena
    @PutMapping("/exit/{uuidStr}")
    public Ticket exitParking(@PathVariable String uuidStr) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
        	log.warn("Nevažeći UUID pokušan za izlaz: {}", uuidStr);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Neispravan UUID");
        }
        Ticket ticket = ticketRepository.findByTicketUuid(uuid)
                .orElseThrow(() -> {log.warn("Karta za UUID nije pronađena: {}", uuidStr);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Karta nije pronađena");
            });

        if (ticket.getExitTime() != null) {
        	log.warn("Pokušaj izlaza sa već iskorištenom kartom: {}", uuidStr);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Karta je već iskorištena");
        }

        ticket.setExitTime(LocalDateTime.now());
        
        
     // Izračun cijene
        long hours = Duration.between(ticket.getEntryTime(), ticket.getExitTime()).toHours();
        if (hours == 0) hours = 1;
        ticket.setPrice(hours * PRICE_PER_HOUR_EUR);
        ticket.setCurrency("EUR");  // Dodaj valutu
        ticket.setPaid(true);
        
        Ticket saved = ticketRepository.save(ticket);
        log.info("Karta izašla: {} | Cijena: {}", saved.getTicketUuid(), saved.getPrice());
        eventPublisher.publishEvent("EXIT", saved);
        return saved;
    }

    //dohvat svih karata (admin)
    @GetMapping
    public List<Ticket> getAllTickets() {
    	log.info("Dohvat svih karata. Ukupno: {}", ticketRepository.count());
        return ticketRepository.findAll();
    }

    //dohvat karte po UUID
    @GetMapping("/{uuidStr}")
    public Ticket getTicketByUuid(@PathVariable String uuidStr) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
        	log.warn("Pokušaj pretrage s nevažećim UUID-om: {}", uuidStr);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Neispravan UUID");
        }

        Ticket ticket = ticketRepository.findByTicketUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Ticket not found for UUID: {}", uuidStr);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Karta nije pronađena");
                });

        log.info("Karta: {}", ticket.getTicketUuid());
        return ticket;
    }
}
