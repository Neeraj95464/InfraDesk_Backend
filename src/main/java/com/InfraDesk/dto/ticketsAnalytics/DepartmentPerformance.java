package com.InfraDesk.dto.ticketsAnalytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentPerformance {
    private String department;
    private Long tickets;
    private Double avgTime;
    private Double sla;
}
