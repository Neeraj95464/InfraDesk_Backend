package com.InfraDesk.controller;

import com.InfraDesk.dto.ticketsAnalytics.*;
import com.InfraDesk.service.TicketAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/v1/analytics/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketAnalyticsController {

    private final TicketAnalyticsService analyticsService;

    @GetMapping("/comprehensive")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ANALYST')")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    @Operation(summary = "Get comprehensive analytics", description = "Returns all analytics data in one response")
    public ResponseEntity<TicketAnalyticsResponse> getComprehensiveAnalytics(
            @Parameter(description = "Company ID") @PathVariable String companyId,
            @Parameter(description = "Date range: 7days, 30days, 90days, 1year")
            @RequestParam(defaultValue = "30days") String dateRange,
            @Parameter(description = "Department filter (optional)")
            @RequestParam(required = false) String department
    ) {
//        log.info("Fetching comprehensive analytics for company: {} {} {} ", department,dateRange,companyId);
        TicketAnalyticsResponse response = analyticsService.getComprehensiveAnalytics(companyId, dateRange, department);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/kpi")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ANALYST')")
//    @Operation(summary = "Get KPI metrics only")
//    public ResponseEntity<KpiMetrics> getKpiMetrics(
//            @RequestParam String companyId,
//            @RequestParam String departmentId,
//            @RequestParam(defaultValue = "30days") String dateRange
//    ) {
//        LocalDateTime[] dates = getDateRangeFromString(dateRange);
//        KpiMetrics metrics = analyticsService.getKpiMetricsOnly(companyId,departmentId, dates[0], dates[1]);
//        return ResponseEntity.ok(metrics);
//    }
//
//    @GetMapping("/status-distribution")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ANALYST', 'AGENT')")
//    @Operation(summary = "Get ticket status distribution")
//    public ResponseEntity<List<StatusDistribution>> getStatusDistribution(
//            @RequestParam String companyId,
//            @RequestParam(defaultValue = "30days") String dateRange
//    ) {
//        LocalDateTime[] dates = getDateRangeFromString(dateRange);
//        List<StatusDistribution> distribution = analyticsService.getStatusDistributionOnly(companyId, dates[0], dates[1]);
//        return ResponseEntity.ok(distribution);
//    }
//
//    @GetMapping("/priority-distribution")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ANALYST', 'AGENT')")
//    @Operation(summary = "Get ticket priority distribution")
//    public ResponseEntity<List<PriorityDistribution>> getPriorityDistribution(
//            @RequestParam String companyId,
//            @RequestParam(defaultValue = "30days") String dateRange
//    ) {
//        LocalDateTime[] dates = getDateRangeFromString(dateRange);
//        List<PriorityDistribution> distribution = analyticsService.getPriorityDistributionOnly(companyId, dates[0], dates[1]);
//        return ResponseEntity.ok(distribution);
//    }
//
//    @GetMapping("/department-performance")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ANALYST')")
//    @Operation(summary = "Get department performance metrics")
//    public ResponseEntity<List<DepartmentPerformance>> getDepartmentPerformance(
//            @RequestParam String companyId,
//            @RequestParam(defaultValue = "30days") String dateRange
//    ) {
//        LocalDateTime[] dates = getDateRangeFromString(dateRange);
//        List<DepartmentPerformance> performance = analyticsService.getDepartmentPerformanceOnly(companyId, dates[0], dates[1]);
//        return ResponseEntity.ok(performance);
//    }
//
//    @GetMapping("/top-performers")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
//    @Operation(summary = "Get top performing assignees")
//    public ResponseEntity<List<AssigneePerformance>> getTopPerformers(
//            @RequestParam String companyId,
//            @RequestParam(defaultValue = "30days") String dateRange,
//            @RequestParam(defaultValue = "10") int limit
//    ) {
//        LocalDateTime[] dates = getDateRangeFromString(dateRange);
//        List<AssigneePerformance> performers = analyticsService.getTopPerformers(companyId, dates[0], dates[1], limit);
//        return ResponseEntity.ok(performers);
//    }
//
//    @GetMapping("/export/pdf")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
//    @Operation(summary = "Export analytics report as PDF")
//    public ResponseEntity<byte[]> exportPdfReport(
//            @RequestParam String companyId,
//            @RequestParam(defaultValue = "30days") String dateRange
//    ) {
//        // TODO: Implement PDF generation using iText or similar library
//        log.info("PDF export requested for company: {}", companyId);
//        return ResponseEntity.ok()
//                .header("Content-Type", "application/pdf")
//                .header("Content-Disposition", "attachment; filename=ticket-analytics-report.pdf")
//                .body(new byte[0]); // Placeholder
//    }
//
//    @GetMapping("/export/excel")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
//    @Operation(summary = "Export analytics report as Excel")
//    public ResponseEntity<byte[]> exportExcelReport(
//            @RequestParam String companyId,
//            @RequestParam(defaultValue = "30days") String dateRange
//    ) {
//        // TODO: Implement Excel generation using Apache POI
//        log.info("Excel export requested for company: {}", companyId);
//        return ResponseEntity.ok()
//                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//                .header("Content-Disposition", "attachment; filename=ticket-analytics-report.xlsx")
//                .body(new byte[0]); // Placeholder
//    }
//
//    @GetMapping("/dashboard/summary")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
//    @Operation(summary = "Get dashboard summary for quick view")
//    public ResponseEntity<DashboardSummary> getDashboardSummary(
//            @RequestParam String companyId
//    ) {
//        LocalDateTime[] dates = getDateRangeFromString("30days");
//        KpiMetrics kpi = analyticsService.getKpiMetricsOnly(companyId, dates[0], dates[1]);
//
//        DashboardSummary summary = DashboardSummary.builder()
//                .totalTickets(kpi.getTotalTickets())
//                .openTickets(kpi.getOpenTickets())
//                .resolvedTickets(kpi.getResolvedTickets())
//                .slaCompliance(kpi.getSlaCompliance())
//                .avgResolutionTime(kpi.getAvgResolutionTime())
//                .build();
//
//        return ResponseEntity.ok(summary);
//    }
//
//    @GetMapping("/trends/weekly")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ANALYST')")
//    @Operation(summary = "Get weekly ticket trends")
//    public ResponseEntity<TicketAnalyticsResponse> getWeeklyTrends(
//            @RequestParam String companyId
//    ) {
//        TicketAnalyticsResponse response = analyticsService.getComprehensiveAnalytics(companyId, "7days", null);
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/trends/monthly")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ANALYST')")
//    @Operation(summary = "Get monthly ticket trends")
//    public ResponseEntity<TicketAnalyticsResponse> getMonthlyTrends(
//            @RequestParam String companyId
//    ) {
//        TicketAnalyticsResponse response = analyticsService.getComprehensiveAnalytics(companyId, "30days", null);
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/realtime/stats")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
//    @Operation(summary = "Get real-time statistics (updated every minute)")
//    public ResponseEntity<RealtimeStats> getRealtimeStats(
//            @RequestParam String companyId
//    ) {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
//
//        KpiMetrics kpi = analyticsService.getKpiMetricsOnly(companyId, startOfDay, now);
//
//        RealtimeStats stats = RealtimeStats.builder()
//                .currentOpenTickets(kpi.getOpenTickets())
//                .ticketsCreatedToday(kpi.getTotalTickets())
//                .ticketsResolvedToday(kpi.getResolvedTickets())
//                .avgResponseTimeToday(kpi.getAvgResolutionTime())
//                .timestamp(LocalDateTime.now())
//                .build();
//
//        return ResponseEntity.ok(stats);
//    }
//
//    @GetMapping("/custom-date-range")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ANALYST')")
//    @Operation(summary = "Get analytics for custom date range")
//    public ResponseEntity<TicketAnalyticsResponse> getCustomDateRangeAnalytics(
//            @RequestParam String companyId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
//            @RequestParam(required = false) String department
//    ) {
//        log.info("Fetching analytics for custom date range: {} to {}", startDate, endDate);
//
//        // Create a custom date range string for the service
//        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
//        String dateRange = daysBetween + "days";
//
//        TicketAnalyticsResponse response = analyticsService.getComprehensiveAnalytics(companyId, dateRange, department);
//        return ResponseEntity.ok(response);
//    }
//
//    // Helper method
//    private LocalDateTime[] getDateRangeFromString(String dateRange) {
//        LocalDateTime endDate = LocalDateTime.now();
//        LocalDateTime startDate;
//
//        switch (dateRange.toLowerCase()) {
//            case "7days":
//                startDate = endDate.minusDays(7);
//                break;
//            case "30days":
//                startDate = endDate.minusDays(30);
//                break;
//            case "90days":
//                startDate = endDate.minusDays(90);
//                break;
//            case "1year":
//                startDate = endDate.minusYears(1);
//                break;
//            default:
//                startDate = endDate.minusDays(30);
//        }
//
//        return new LocalDateTime[]{startDate, endDate};
//    }
}

