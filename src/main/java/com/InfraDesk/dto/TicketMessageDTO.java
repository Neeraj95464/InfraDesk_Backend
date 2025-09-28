package com.InfraDesk.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessageDTO {
    private String publicId;
    private String ticketId;
    private String authorId;
    private String authorName;
    private String body;
    private Boolean internalNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttachmentDTO> attachments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttachmentDTO {
        private String publicId;
        private String fileName;
        private String fileUrl;
    }
}
