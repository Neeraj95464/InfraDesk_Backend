package com.InfraDesk.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketFeedbackDTO {
    private String ticketPublicId;
    private String assigneeName;
    private String creatorName;
    private Integer stars;
    private String feedbackText;
    private LocalDateTime submittedAt;
}

