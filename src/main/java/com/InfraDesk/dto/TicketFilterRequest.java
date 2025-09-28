package com.InfraDesk.dto;

import com.InfraDesk.enums.TicketPriority;
import com.InfraDesk.enums.TicketStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class TicketFilterRequest {
    private TicketStatus status;
    private TicketPriority priority;
    private String departmentId;
    private String createdByUserId;
    private String assigneeUserId;
    private String locationId;
    private String ticketTypeId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdEnd;

    private String keyword; // subject/description search
    private int page = 0;
    private int size = 10;
}
