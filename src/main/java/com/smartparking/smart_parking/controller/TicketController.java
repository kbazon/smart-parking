package com.smartparking.smart_parking.controller;

import com.smartparking.smart_parking.model.Ticket;
import com.smartparking.smart_parking.repository.TicketRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    //generiranje karte za ulaz
    @PostMapping("/entry")
    public Ticket createTicket() {
        Ticket ticket = new Ticket(); // UUID i entryTime auto
        return ticketRepository.save(ticket);
    }

    // izlazak i cijena
    @PutMapping("/exit/{uuid}")
    public Ticket exitParking(@PathVariable UUID uuid) {
        Ticket ticket = ticketRepository.findByTicketUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getExitTime() != null) {
            throw new RuntimeException("Ticket already used");
        }

        ticket.setExitTime(LocalDateTime.now());

        //cijena
        long hours = Duration.between(ticket.getEntryTime(), ticket.getExitTime()).toHours();
        if (hours == 0) hours = 1;
        ticket.setPrice(hours * 5.0);

        ticket.setPaid(true); //placeno
        return ticketRepository.save(ticket);
    }

    //dohvat svih karata(admin)
    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    //dohvat karte po UUID
    @GetMapping("/{uuid}")
    public Ticket getTicketByUuid(@PathVariable UUID uuid) {
        return ticketRepository.findByTicketUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }
}
