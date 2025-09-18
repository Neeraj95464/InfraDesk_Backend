package com.InfraDesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO {

    private String publicId;
    private String companyId;
    private String createdByUserId;
    private String assigneeUserId;
    private String subject;
    private String description;
    private String status;
    private String priority;
    private TicketTypeDTO ticketType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime slaDueDate;
}


