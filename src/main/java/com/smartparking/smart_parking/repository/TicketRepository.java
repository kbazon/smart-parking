package com.smartparking.smart_parking.repository;

import com.smartparking.smart_parking.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketUuid(UUID ticketUuid);
}
