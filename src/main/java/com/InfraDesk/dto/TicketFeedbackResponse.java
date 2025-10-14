package com.InfraDesk.dto;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketFeedbackResponse {
    private Long id;
    private Integer stars;
    private String feedbackText;
    private LocalDateTime submittedAt;
}