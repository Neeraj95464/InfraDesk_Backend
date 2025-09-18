package com.InfraDesk.mapper;

import com.InfraDesk.dto.TicketDTO;
import com.InfraDesk.dto.TicketTypeDTO;
import com.InfraDesk.entity.Ticket;

public class TicketMapper {

    public static TicketDTO toDto(Ticket ticket) {
        if (ticket == null) return null;

        return TicketDTO.builder()
                .publicId(ticket.getPublicId())
                .companyId(ticket.getCompany().getPublicId())
                .createdByUserId(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getPublicId() : null)
                .assigneeUserId(ticket.getAssignee() != null ? ticket.getAssignee().getPublicId() : null)
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus() != null ? ticket.getStatus().name() : null)
                .priority(ticket.getPriority() != null ? ticket.getPriority().name() : null)
                .ticketType(mapTicketType(ticket.getTicketType()))
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .slaDueDate(ticket.getSlaDueDate())
                .build();
    }

    private static TicketTypeDTO mapTicketType(com.InfraDesk.entity.TicketType ticketType) {
        if (ticketType == null) return null;
        return TicketTypeDTO.builder()
                .publicId(ticketType.getPublicId())
                .name(ticketType.getName())
                .description(ticketType.getDescription())
                .build();
    }
}

