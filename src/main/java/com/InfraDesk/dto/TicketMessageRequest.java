package com.InfraDesk.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessageRequest {
    private String ticketId;
    private String body;
    private Boolean internalNote = Boolean.FALSE;
    private String senderEmail;

    // for uploaded files
    private List<MultipartFile> attachments;
}
