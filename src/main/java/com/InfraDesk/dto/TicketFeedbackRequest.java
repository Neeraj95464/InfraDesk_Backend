package com.InfraDesk.dto;

import lombok.Data;

@Data
public class TicketFeedbackRequest {
    private Integer stars;         // 1 to 5
    private String feedbackText;   // optional
}
