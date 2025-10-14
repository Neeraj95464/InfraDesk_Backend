package com.InfraDesk.dto.ticketsAnalytics;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictiveInsights {
    private Long expectedNextWeek;
    private Double growthPercentage;
    private String peakLoadTime;
    private Long slaRiskTickets;
    private List<ForecastData> forecastData;
}

