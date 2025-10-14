package com.InfraDesk.dto.ticketsAnalytics;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketAnalyticsResponse {
    private KpiMetrics kpiMetrics;
    private List<StatusDistribution> ticketsByStatus;
    private List<PriorityDistribution> ticketsByPriority;
    private List<TrendData> ticketTrend;
    private List<DepartmentPerformance> departmentPerformance;
    private List<TicketTypeDistribution> ticketTypeDistribution;
    private List<ResolutionTimeByPriority> resolutionTimeByPriority;
    private List<AssigneePerformance> assigneePerformance;
    private List<HourlyVolume> hourlyTicketVolume;
    private List<SlaComplianceDept> slaComplianceByDepartment;
    private List<PerformanceMetric> performanceRadar;
    private List<TicketAgeDistribution> ticketAgeDistribution;
    private List<SatisfactionTrend> satisfactionTrend;
    private List<LocationDistribution> locationDistribution;
    private List<ResponseTimeData> responseTimeData;
    private EscalationAnalysis escalationAnalysis;
    private List<ReopenedTickets> reopenedTickets;
    private PredictiveInsights predictiveInsights;
    private List<HeatmapData> responseTimeHeatmap;
}

