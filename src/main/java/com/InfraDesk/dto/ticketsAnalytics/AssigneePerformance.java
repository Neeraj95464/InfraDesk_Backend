package com.InfraDesk.dto.ticketsAnalytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssigneePerformance {
    private String name;
    private Long resolved;
    private Long pending;
    private Double avgTime;
    private Double satisfaction;
}
