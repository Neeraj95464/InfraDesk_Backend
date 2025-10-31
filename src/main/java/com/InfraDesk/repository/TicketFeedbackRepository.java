package com.InfraDesk.repository;

import com.InfraDesk.entity.TicketFeedback;
import com.InfraDesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TicketFeedbackRepository extends JpaRepository<TicketFeedback, Long>, JpaSpecificationExecutor<TicketFeedback> {
    Optional<TicketFeedback> findByTicket(Ticket ticket);
}
