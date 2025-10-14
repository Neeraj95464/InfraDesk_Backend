package com.InfraDesk.dto.ticketsAnalytics;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeStats {
    private Long currentOpenTickets;
    private Long ticketsCreatedToday;
    private Long ticketsResolvedToday;
    private Double avgResponseTimeToday;
    private LocalDateTime timestamp;
}
