package com.InfraDesk.dto.ticketsAnalytics;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationAnalysis {
    private Long notEscalated;
    private Long escalatedL2;
    private Long escalatedL3;
}
