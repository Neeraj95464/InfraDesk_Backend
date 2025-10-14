package com.InfraDesk.dto.ticketsAnalytics;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeDistribution {
    private String type;
    private Long count;
    private Double percentage;
}
