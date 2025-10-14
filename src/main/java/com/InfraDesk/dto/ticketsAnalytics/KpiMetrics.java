package com.InfraDesk.dto.ticketsAnalytics;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiMetrics {
    private Long totalTickets;
    private Long openTickets;
    private Long resolvedTickets;
    private Double avgResolutionTime;
    private Double slaCompliance;
    private Double ticketGrowth;
}