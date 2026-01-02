package com.smartparking.smart_parking.controller;

import com.smartparking.smart_parking.model.Ticket;
import com.smartparking.smart_parking.repository.TicketRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @GetMapping("/{id}")
    public Ticket getTicket(@PathVariable Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @PutMapping("/{id}/pay")
    public Ticket payTicket(@PathVariable Long id) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        ticket.setPaid(true);
        return ticketRepository.save(ticket);
    }

    @DeleteMapping("/{id}")
    public void deleteTicket(@PathVariable Long id) {
        ticketRepository.deleteById(id);
    }
}