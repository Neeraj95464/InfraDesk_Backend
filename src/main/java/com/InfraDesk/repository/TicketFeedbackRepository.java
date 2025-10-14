package com.InfraDesk.repository;

import com.InfraDesk.entity.TicketFeedback;
import com.InfraDesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketFeedbackRepository extends JpaRepository<TicketFeedback, Long> {
    Optional<TicketFeedback> findByTicket(Ticket ticket);
}
