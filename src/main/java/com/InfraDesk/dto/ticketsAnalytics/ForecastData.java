package com.InfraDesk.dto.ticketsAnalytics;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastData {
    private String date;
    private Long historical;
    private Long predicted;
}
