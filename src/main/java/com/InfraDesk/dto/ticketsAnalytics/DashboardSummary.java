package com.InfraDesk.dto.ticketsAnalytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Additional DTOs for controller responses
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private Long totalTickets;
    private Long openTickets;
    private Long resolvedTickets;
    private Double slaCompliance;
    private Double avgResolutionTime;
}