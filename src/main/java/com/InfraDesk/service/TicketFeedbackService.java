package com.InfraDesk.service;

import com.InfraDesk.dto.TicketFeedbackRequest;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.TicketFeedback;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.TicketFeedbackRepository;
import com.InfraDesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TicketFeedbackService {
    private final TicketRepository ticketRepository;
    private final TicketFeedbackRepository feedbackRepository;

    @Transactional
    public TicketFeedback submitFeedback(String ticketPublicId, TicketFeedbackRequest request) {
        Ticket ticket = ticketRepository.findByPublicId(ticketPublicId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Optionally prevent duplicate feedback per ticket
        feedbackRepository.findByTicket(ticket).ifPresent(existing -> {
            throw new RuntimeException("Feedback already submitted for this ticket");
        });

        TicketFeedback feedback = TicketFeedback.builder()
                .ticket(ticket)
                .stars(request.getStars())
                .feedbackText(request.getFeedbackText())
                .build();
        return feedbackRepository.save(feedback);
    }



    @Transactional
    public void saveOrUpdateFeedbackStarsOnly(String ticketId, Integer stars) {
        Ticket ticket = ticketRepository.findByPublicId(ticketId)
                .orElseThrow(() -> new BusinessException("Ticket not found"));

        // Save as new record or update existing feedback's stars if it exists
        TicketFeedback feedback = feedbackRepository.findByTicket(ticket)
                .orElse(TicketFeedback.builder().ticket(ticket).build());

        feedback.setStars(stars);
        feedbackRepository.save(feedback);
    }

    @Transactional
    public void saveOrUpdateFeedback(String ticketId, Integer stars, String feedbackText) {
        Ticket ticket = ticketRepository.findByPublicId(ticketId)
                .orElseThrow(() -> new BusinessException("Ticket not found"));

        TicketFeedback feedback = feedbackRepository.findByTicket(ticket)
                .orElse(TicketFeedback.builder().ticket(ticket).build());

        feedback.setStars(stars);
        feedback.setFeedbackText(feedbackText);
        feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public boolean existsByTicketAndStars(String ticketId, Integer stars) {
        Ticket ticket = ticketRepository.findByPublicId(ticketId).orElse(null);
        if (ticket == null) return false;
        return feedbackRepository.findByTicket(ticket)
                .map(fb -> Objects.equals(fb.getStars(), stars))
                .orElse(false);
    }

}

