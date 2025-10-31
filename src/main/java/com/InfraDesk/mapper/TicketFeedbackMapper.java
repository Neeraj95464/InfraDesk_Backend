package com.InfraDesk.mapper;

import com.InfraDesk.dto.TicketFeedbackDTO;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.TicketFeedback;

// Static DTO mapper method
public class TicketFeedbackMapper {

    public static TicketFeedbackDTO toDTO(TicketFeedback feedback) {
        Ticket ticket = feedback.getTicket();

        String assigneeName = null;
        if (ticket != null && ticket.getAssignee() != null) {
            assigneeName = ticket.getAssignee().getEmail();
        }

        return TicketFeedbackDTO.builder()
                .ticketPublicId(ticket != null ? ticket.getPublicId() : null)
//                .assigneeName(assigneeName)
                .assigneeName(
                        (ticket.getAssignee() != null
                                && ticket.getAssignee().getEmployeeProfiles() != null
                                && !ticket.getAssignee().getEmployeeProfiles().isEmpty())
                                ? ticket.getAssignee().getEmployeeProfiles().get(0).getName()  // use employee name
                                : (ticket.getAssignee() != null ? ticket.getAssignee().getEmail() : "")
                )
                .creatorName(
                        (ticket.getCreatedBy() != null
                                && ticket.getCreatedBy().getEmployeeProfiles() != null
                                && !ticket.getCreatedBy().getEmployeeProfiles().isEmpty())
                                ? ticket.getCreatedBy().getEmployeeProfiles().get(0).getName()  // use employee name
                                : (ticket.getCreatedBy() != null ? ticket.getCreatedBy().getEmail() : "")
                )
                .stars(feedback.getStars())
                .feedbackText(feedback.getFeedbackText())
                .submittedAt(feedback.getSubmittedAt())
                .build();
    }
}
