package com.InfraDesk.dto;

import com.InfraDesk.enums.TicketPriority;
import com.InfraDesk.enums.TicketStatus;
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
    private String createdByUserName;
    private String assigneeUserId;
    private String assigneeUserName;
    private String locationId;
    private String locationName;
    private String departmentId;
    private String departmentName;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private String ticketTypeName;
    private String ticketTypeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime slaDueDate;
}


