package com.InfraDesk.dto;

import com.InfraDesk.enums.TicketPriority;
import com.InfraDesk.enums.TicketStatus;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateTicketRequest {
    private String subject;
    private String creatorEmail;
    private String description;
    private String ticketTypeId;
    private String departmentId;
    private String locationId;
    private TicketPriority priority;
    private TicketStatus status;
    private List<String> ccUserIds;
    // attachments provided by controller via @RequestPart
    private List<MultipartFile> attachments;
}



