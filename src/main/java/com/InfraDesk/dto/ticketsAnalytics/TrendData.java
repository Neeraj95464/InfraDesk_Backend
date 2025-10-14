package com.InfraDesk.dto.ticketsAnalytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendData {
    private String date;
    private Long created;
    private Long resolved;
    private Long open;
}