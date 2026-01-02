package com.smartparking.smart_parking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets", schema = "public")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_uuid", unique = true, nullable = false)
    private UUID ticketUuid = UUID.randomUUID();

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime = LocalDateTime.now();

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(name = "price")
    private Double price;

    @Column(name = "paid")
    private Boolean paid = false;

    public Long getId() { return id; }
    public UUID getTicketUuid() { return ticketUuid; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public Double getPrice() { return price; }
    public Boolean getPaid() { return paid; }

    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    public void setPrice(Double price) { this.price = price; }
    public void setPaid(Boolean paid) { this.paid = paid; }
}
