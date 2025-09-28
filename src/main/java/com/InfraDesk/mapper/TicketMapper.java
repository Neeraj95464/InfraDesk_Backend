//package com.InfraDesk.mapper;
//
//import com.InfraDesk.dto.TicketDTO;
//import com.InfraDesk.dto.TicketTypeDTO;
//import com.InfraDesk.entity.Ticket;
//
//public class TicketMapper {
//
//    public static TicketDTO toDto(Ticket ticket) {
//        if (ticket == null) return null;
//
//        return TicketDTO.builder()
//                .publicId(ticket.getPublicId())
//                .companyId(ticket.getCompany().getPublicId())
//                .createdByUserId(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getPublicId() : null)
//                .assigneeUserId(ticket.getAssignee() != null ? ticket.getAssignee().getPublicId() : null)
//                .subject(ticket.getSubject())
//                .description(ticket.getDescription())
//                .status(ticket.getStatus() != null ? ticket.getStatus().name() : null)
//                .priority(ticket.getPriority() != null ? ticket.getPriority().name() : null)
//                .ticketType(mapTicketType(ticket.getTicketType()))
//                .createdAt(ticket.getCreatedAt())
//                .updatedAt(ticket.getUpdatedAt())
//                .slaDueDate(ticket.getSlaDueDate())
//                .build();
//    }
//
//    private static TicketTypeDTO mapTicketType(com.InfraDesk.entity.TicketType ticketType) {
//        if (ticketType == null) return null;
//        return TicketTypeDTO.builder()
//                .publicId(ticketType.getPublicId())
//                .name(ticketType.getName())
//                .description(ticketType.getDescription())
//                .build();
//    }
//}
//



package com.InfraDesk.mapper;

import com.InfraDesk.dto.TicketDTO;
import com.InfraDesk.entity.Ticket;

public class TicketMapper {

    public static TicketDTO toDto(Ticket ticket) {
        if (ticket == null) return null;

        return TicketDTO.builder()
                .publicId(ticket.getPublicId())
                .companyId(ticket.getCompany().getPublicId())

                .createdByUserId(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getPublicId() : null)
                .createdByUserName(ticket.getCreatedBy() != null ?
                        ticket.getCreatedBy().getEmployeeProfiles().getFirst().getName() : null)

                .assigneeUserId(ticket.getAssignee() != null ? ticket.getAssignee().getPublicId() : null)
                .assigneeUserName(ticket.getAssignee() != null ?
                        ticket.getAssignee().getEmployeeProfiles().getFirst().getName() : null)

                .locationId(ticket.getLocation() != null ? ticket.getLocation().getPublicId() : null)
                .locationName(ticket.getLocation() != null ? ticket.getLocation().getName() : null)

                .departmentId(ticket.getDepartment() != null ? ticket.getDepartment().getPublicId() : null)
                .departmentName(ticket.getDepartment() != null ? ticket.getDepartment().getName() : null)

                .subject(ticket.getSubject())
                .description(ticket.getDescription())

                .status(ticket.getStatus() != null ? ticket.getStatus() : null)
                .priority(ticket.getPriority() != null ? ticket.getPriority() : null)

                .ticketTypeId(ticket.getTicketType() != null ? ticket.getTicketType().getPublicId() : null)
                .ticketTypeName(ticket.getTicketType() != null ? ticket.getTicketType().getName() : null)

                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .slaDueDate(ticket.getSlaDueDate())
                .build();
    }
}
