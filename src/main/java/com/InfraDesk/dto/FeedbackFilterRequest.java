package com.InfraDesk.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackFilterRequest {
    private String assigneeUserId;  // Filter by ticket assignee user id
    private Integer stars;          // Filter by rating stars (1 to 5)
    private LocalDateTime fromDate; // Filter from feedback submittedAt >= fromDate
    private LocalDateTime toDate;   // Filter feedback submittedAt <= toDate
    private String keyword;         // Keyword search on feedbackText
}

