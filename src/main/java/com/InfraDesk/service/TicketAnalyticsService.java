

package com.InfraDesk.service;

import com.InfraDesk.dto.ticketsAnalytics.*;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Department;
import com.InfraDesk.enums.TicketPriority;
import com.InfraDesk.enums.TicketStatus;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.DepartmentRepository;
import com.InfraDesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TicketAnalyticsService {

    private final TicketRepository ticketRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;

    public TicketAnalyticsResponse getComprehensiveAnalytics(String companyPublicId, String dateRange, String departmentFilter) {
        LocalDateTime[] dates = getDateRange(dateRange);
        LocalDateTime startDate = dates[0];
        LocalDateTime endDate = dates[1];

        Long companyId = resolveCompanyId(companyPublicId);
        Long departmentId = resolveDepartmentId(departmentFilter, companyPublicId); // 👈 new helper

        return TicketAnalyticsResponse.builder()
                .kpiMetrics(getKpiMetrics(companyId, departmentId, startDate, endDate))
                .ticketsByStatus(getStatusDistribution(companyId, departmentId, startDate, endDate))
                .ticketsByPriority(getPriorityDistribution(companyId, departmentId, startDate, endDate))
                .ticketTrend(getTicketTrend(companyId, departmentId, startDate, endDate))
                .departmentPerformance(getDepartmentPerformance(companyId, startDate, endDate))
                .ticketTypeDistribution(getTicketTypeDistribution(companyId, departmentId, startDate, endDate))
                .resolutionTimeByPriority(getResolutionTimeByPriority(companyId, departmentId, startDate, endDate))
                .assigneePerformance(getAssigneePerformance(companyId, departmentId, startDate, endDate))
                .hourlyTicketVolume(getHourlyTicketVolume(companyId, departmentId, startDate, endDate))
                .slaComplianceByDepartment(getSlaComplianceByDepartment(companyId,departmentId, startDate, endDate))
                .performanceRadar(getPerformanceRadar(companyId, departmentId, startDate, endDate))
                .ticketAgeDistribution(getTicketAgeDistribution(companyId, departmentId))
                .satisfactionTrend(getSatisfactionTrend(companyId,departmentId, startDate, endDate))
                .locationDistribution(getLocationDistribution(companyId, departmentId, startDate, endDate))
                .responseTimeData(getResponseTimeData(companyId,departmentId, startDate, endDate))
                .escalationAnalysis(getEscalationAnalysis(companyId, departmentId, startDate, endDate))
                .reopenedTickets(getReopenedTickets(companyId, departmentId, startDate, endDate))
                .predictiveInsights(getPredictiveInsights(companyId, departmentId, startDate, endDate))
                .responseTimeHeatmap(getResponseTimeHeatmap(companyId,departmentId, startDate, endDate))
                .build();
    }


    /* ----------------------- Helpers & Core ----------------------- */

    private Long resolveCompanyId(String companyPublicId) {
        return companyRepository.findByPublicId(companyPublicId)
                .map(Company::getId)
                .orElseThrow(() -> new BusinessException("Company not found for publicId: " + companyPublicId));
    }

    private double safeRound(Double value, int precision) {
        if (value == null) return 0.0;
        double scale = Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }

    private Long resolveDepartmentId(String departmentId, String companyId) {
        if (departmentId == null || departmentId.equalsIgnoreCase("ALL"))
            return null;

        return departmentRepository.findByPublicIdAndCompany_PublicId(departmentId, companyId)
                .map(Department::getId)
                .orElseThrow(() -> new BusinessException("Department not found: " + departmentId));
    }


    /* ----------------------- KPI ----------------------- */

    private KpiMetrics getKpiMetrics(Long companyId, Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Computing KPI metrics for companyId={}, start={}, end={}", companyId, startDate, endDate);

        Long totalTickets = ticketRepository.countByCompanyAndOptionalDepartment(companyId,departmentId);
        Long openTickets = ticketRepository.countByCompanyAndStatusAndOptionalDepartment(companyId, TicketStatus.OPEN,departmentId);
        Long resolvedTickets = ticketRepository.countByCompanyAndStatusAndOptionalDepartment(companyId, TicketStatus.RESOLVED,departmentId);

        Double avgResolutionTime = ticketRepository.getAverageResolutionTime(companyId,departmentId, startDate, endDate);
        Double slaCompliance = ticketRepository.getSlaCompliancePercentage(companyId, departmentId, startDate, endDate);

        // previous period: same length immediately before startDate
        long daysBetween = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
        if (daysBetween <= 0) daysBetween = 1;
        LocalDateTime prevStart = startDate.minusDays(daysBetween);
        LocalDateTime prevEnd = startDate;

        Long previousPeriodTickets = ticketRepository.countByCompanyAndOptionalDepartment(companyId,departmentId); // implement repo method or fallback
        double ticketGrowth = (previousPeriodTickets != null && previousPeriodTickets > 0)
                ? ((double)(totalTickets - previousPeriodTickets) * 100.0 / previousPeriodTickets)
                : 0.0;

        return KpiMetrics.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .resolvedTickets(resolvedTickets)
                .avgResolutionTime(safeRound(avgResolutionTime, 1))
                .slaCompliance(safeRound(slaCompliance, 1))
                .ticketGrowth(safeRound(ticketGrowth, 1))
                .build();
    }

    /* ----------------------- Status distribution ----------------------- */

    private List<StatusDistribution> getStatusDistribution(Long companyId,Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = ticketRepository.getStatusDistribution(companyId, startDate, endDate);

        Map<String, String> colorMap = Map.of(
                "OPEN", "#3b82f6",
                "IN_PROGRESS", "#f59e0b",
                "RESOLVED", "#10b981",
                "CLOSED", "#6b7280",
                "ON_HOLD", "#ef4444"
        );

        return results.stream()
                .map(row -> {
                    TicketStatus status = (TicketStatus) row.get("status");
                    Number countN = (Number) row.get("count");
                    long count = countN != null ? countN.longValue() : 0L;
                    String name = status != null ? status.name() : "UNKNOWN";
                    return StatusDistribution.builder()
                            .name(name)
                            .value(count)
                            .color(colorMap.getOrDefault(name, "#6b7280"))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /* ----------------------- Priority distribution ----------------------- */

    private List<PriorityDistribution> getPriorityDistribution(Long companyId, Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = ticketRepository.getPriorityDistribution(companyId, startDate, endDate);

        Map<String, String> colorMap = Map.of(
                "CRITICAL", "#dc2626",
                "HIGH", "#f59e0b",
                "MEDIUM", "#3b82f6",
                "LOW", "#10b981"
        );

        return results.stream()
                .map(row -> {
                    TicketPriority priority = (TicketPriority) row.get("priority");
                    Number countN = (Number) row.get("count");
                    long count = countN != null ? countN.longValue() : 0L;
                    String name = priority != null ? priority.name() : "UNKNOWN";
                    return PriorityDistribution.builder()
                            .name(name)
                            .value(count)
                            .color(colorMap.getOrDefault(name, "#6b7280"))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /* ----------------------- Ticket trend ----------------------- */

    private List<TrendData> getTicketTrend(Long companyId, Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> createdData = ticketRepository.getTicketTrendByDate(companyId, startDate, endDate);

        // Group by date label (e.g., "MMM dd") preserving order by date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        Map<String, Long> createdByLabel = new LinkedHashMap<>();

        createdData.stream()
                .sorted(Comparator.comparing(row -> ((Date) row.get("date")).toLocalDate()))
                .forEach(row -> {
                    Date sqlDate = (Date) row.get("date");
                    long count = ((Number) row.get("count")).longValue();
                    String label = sqlDate.toLocalDate().format(formatter);
                    createdByLabel.merge(label, count, Long::sum);
                });

        return createdByLabel.entrySet().stream()
                .map(entry -> {
                    long created = entry.getValue();
                    long resolved = Math.round(created * 0.9); // placeholder approximation
                    long open = Math.max(0L, created - resolved);
                    return TrendData.builder()
                            .date(entry.getKey())
                            .created(created)
                            .resolved(resolved)
                            .open(open)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /* ----------------------- Department performance ----------------------- */

    private List<DepartmentPerformance> getDepartmentPerformance(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = ticketRepository.getDepartmentPerformance(companyId, startDate, endDate);

        return results.stream()
                .map(row -> {
                    String department = (String) row.get("department");
                    Number ticketsN = (Number) row.get("tickets");
                    Number avgTimeN = (Number) row.get("avgTime");
                    long tickets = ticketsN != null ? ticketsN.longValue() : 0L;
                    double avgTime = avgTimeN != null ? avgTimeN.doubleValue() : 0.0;
                    // TODO: replace SLA mock with real SLA calc
                    double sla = 85.0;
                    return DepartmentPerformance.builder()
                            .department(department)
                            .tickets(tickets)
                            .avgTime(safeRound(avgTime, 1))
                            .sla(sla)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /* ----------------------- Ticket type distribution ----------------------- */

    private List<TicketTypeDistribution> getTicketTypeDistribution(Long companyId, Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = ticketRepository.getTicketTypeDistribution(companyId, startDate, endDate);

        long total = results.stream()
                .mapToLong(row -> ((Number) row.get("count")).longValue())
                .sum();

        return results.stream()
                .map(row -> {
                    long count = ((Number) row.get("count")).longValue();
                    String type = (String) row.get("type");
                    double percent = total > 0 ? (count * 100.0 / total) : 0.0;
                    return TicketTypeDistribution.builder()
                            .type(type)
                            .count(count)
                            .percentage(safeRound(percent, 1))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /* ----------------------- Resolution time by priority ----------------------- */

    private List<ResolutionTimeByPriority> getResolutionTimeByPriority(Long companyId,Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = ticketRepository.getAvgResolutionTimeByPriority(companyId, startDate, endDate);

        return results.stream()
                .map(row -> {
                    TicketPriority p = (TicketPriority) row.get("priority");
                    Number avgN = (Number) row.get("avgHours");
                    return ResolutionTimeByPriority.builder()
                            .priority(p != null ? p.name() : "UNKNOWN")
                            .avgHours(safeRound(avgN != null ? avgN.doubleValue() : 0.0, 1))
                            .build();
                }).collect(Collectors.toList());
    }

    /* ----------------------- Assignee performance ----------------------- */

    private List<AssigneePerformance> getAssigneePerformance(Long companyId,Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = ticketRepository.getAssigneePerformance(companyId, startDate, endDate);

        return results.stream()
                .limit(10)
                .map(row -> {
                    String first = (String) row.get("firstName");
                    String last = (String) row.get("lastName");
                    Number resolvedN = (Number) row.get("resolved");
                    Number pendingN = (Number) row.get("pending");
                    Number avgTimeN = (Number) row.get("avgTime");
                    long resolved = resolvedN != null ? resolvedN.longValue() : 0L;
                    long pending = pendingN != null ? pendingN.longValue() : 0L;
                    double avgTime = avgTimeN != null ? avgTimeN.doubleValue() : 0.0;
                    // TODO: replace mock satisfaction
                    double satisfaction = 4.0;
                    return AssigneePerformance.builder()
                            .name((first != null ? first : "") + " " + (last != null ? last : ""))
                            .resolved(resolved)
                            .pending(pending)
                            .avgTime(safeRound(avgTime, 1))
                            .satisfaction(satisfaction)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /* ----------------------- Hourly volume ----------------------- */

    private List<HourlyVolume> getHourlyTicketVolume(Long companyId,Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = ticketRepository.getHourlyTicketVolume(companyId, startDate, endDate);

        return results.stream()
                .map(row -> {
                    Number hourN = (Number) row.get("hour");
                    Number countN = (Number) row.get("count");
                    int hour = hourN != null ? hourN.intValue() : 0;
                    long count = countN != null ? countN.longValue() : 0L;
                    return HourlyVolume.builder()
                            .hour(String.format("%02d:00", hour))
                            .count(count)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /* ----------------------- SLA compliance by department ----------------------- */

    private List<SlaComplianceDept> getSlaComplianceByDepartment(Long companyId, Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = ticketRepository.getSlaComplianceByDepartment(companyId, startDate, endDate);

        return results.stream()
                .map(row -> SlaComplianceDept.builder()
                        .department((String) row.get("department"))
                        .onTime(((Number) row.get("onTime")).longValue())
                        .breached(((Number) row.get("breached")).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    /* ----------------------- Performance radar ----------------------- */

    private List<PerformanceMetric> getPerformanceRadar(Long companyId, Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        Double slaCompliance = ticketRepository.getSlaCompliancePercentage(companyId, startDate, endDate);
        return Arrays.asList(
                PerformanceMetric.builder().metric("Response Time").value(85.0).build(),
                PerformanceMetric.builder().metric("Resolution Rate").value(92.0).build(),
                PerformanceMetric.builder().metric("SLA Compliance").value(slaCompliance != null ? slaCompliance : 0.0).build(),
                PerformanceMetric.builder().metric("Customer Satisfaction").value(90.0).build(),
                PerformanceMetric.builder().metric("First Contact Resolution").value(78.0).build(),
                PerformanceMetric.builder().metric("Ticket Volume Management").value(82.0).build()
        );
    }

    /* ----------------------- Ticket age distribution ----------------------- */

    private List<TicketAgeDistribution> getTicketAgeDistribution(Long companyId, Long departmentId) {
        List<Map<String, Object>> results = ticketRepository.getTicketAgeDistribution(companyId);

        Map<String, Long> ageMap = new LinkedHashMap<>();
        ageMap.put("0-24h", 0L);
        ageMap.put("1-3 days", 0L);
        ageMap.put("3-7 days", 0L);
        ageMap.put("7-14 days", 0L);
        ageMap.put("14-30 days", 0L);
        ageMap.put("30+ days", 0L);

        results.forEach(row -> {
            String age = (String) row.get("age");
            Number countN = (Number) row.get("count");
            long count = countN != null ? countN.longValue() : 0L;
            if (age != null) {
                ageMap.put(age, count);
            }
        });

        return ageMap.entrySet().stream()
                .map(e -> TicketAgeDistribution.builder().age(e.getKey()).count(e.getValue()).build())
                .collect(Collectors.toList());
    }

    /* ----------------------- Satisfaction trend ----------------------- */

    private List<SatisfactionTrend> getSatisfactionTrend(Long companyId, Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Replace with actual satisfaction survey data retrieval.
        List<SatisfactionTrend> trends = new ArrayList<>();
        LocalDate start = startDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusMonths(1)) {
            trends.add(SatisfactionTrend.builder()
                    .month(d.format(DateTimeFormatter.ofPattern("MMM")))
                    .score(4.0)
                    .target(4.5)
                    .build());
        }

        return trends;
    }

    /* ----------------------- Location distribution ----------------------- */

    private List<LocationDistribution> getLocationDistribution(Long companyId,Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = ticketRepository.getLocationDistribution(companyId, startDate, endDate);

        return results.stream()
                .map(row -> LocationDistribution.builder()
                        .location((String) row.get("location"))
                        .tickets(((Number) row.get("tickets")).longValue())
                        .resolved(((Number) row.get("resolved")).longValue())
                        .pending(((Number) row.get("pending")).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    /* ----------------------- Response time data ----------------------- */

    private List<ResponseTimeData> getResponseTimeData(Long companyId, Long departmentId,LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Implement based on ticket first response and resolution events
        List<ResponseTimeData> data = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            data.add(ResponseTimeData.builder()
                    .week("Week " + (i + 1))
                    .firstResponse(0.5)
                    .resolution(3.5)
                    .build());
        }
        return data;
    }

    /* ----------------------- Escalation analysis ----------------------- */

    private EscalationAnalysis getEscalationAnalysis(Long companyId, Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        long totalTickets = ticketRepository.countByCompanyId(companyId);
        return EscalationAnalysis.builder()
                .notEscalated((long) (totalTickets * 0.93))
                .escalatedL2((long) (totalTickets * 0.05))
                .escalatedL3((long) (totalTickets * 0.02))
                .build();
    }

    /* ----------------------- Reopened tickets ----------------------- */

    private List<ReopenedTickets> getReopenedTickets(Long companyId,Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<ReopenedTickets> data = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            data.add(ReopenedTickets.builder()
                    .month(LocalDate.now().minusMonths(6 - i).format(DateTimeFormatter.ofPattern("MMM")))
                    .reopened(5L)
                    .total(180L)
                    .build());
        }
        return data;
    }

    /* ----------------------- Predictive insights ----------------------- */

    private PredictiveInsights getPredictiveInsights(Long companyId, Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> recentTrend = ticketRepository.getTicketTrendByDate(companyId, endDate.minusDays(14), endDate);

        long recentAvg = (long) recentTrend.stream()
                .mapToLong(row -> ((Number) row.get("count")).longValue())
                .average()
                .orElse(40.0);

        long expectedNextWeek = recentAvg * 7;

        List<ForecastData> forecastData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            forecastData.add(ForecastData.builder()
                    .date(getDayName(i))
                    .historical(recentAvg)
                    .predicted(null)
                    .build());
        }
        for (int i = 0; i < 5; i++) {
            forecastData.add(ForecastData.builder()
                    .date(getDayName(i))
                    .historical(null)
                    .predicted((long) (recentAvg * 0.92))
                    .build());
        }

        return PredictiveInsights.builder()
                .expectedNextWeek(expectedNextWeek)
                .growthPercentage(0.0)
                .peakLoadTime("Thursday 14:00")
                .slaRiskTickets(18L)
                .forecastData(forecastData)
                .build();
    }

    /* ----------------------- Response time heatmap ----------------------- */

    private List<HeatmapData> getResponseTimeHeatmap(Long companyId, Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<HeatmapData> heatmap = new ArrayList<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (String day : days) {
            for (int hour = 0; hour < 24; hour += 3) {
                heatmap.add(HeatmapData.builder()
                        .day(day)
                        .hour(String.format("%02d:00", hour))
                        .avgResponseTime(0.0)
                        .build());
            }
        }

        return heatmap;
    }

    /* ----------------------- Utilities ----------------------- */

    private LocalDateTime[] getDateRange(String dateRange) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;

        switch (dateRange == null ? "30days" : dateRange.toLowerCase()) {
            case "7days":
                startDate = endDate.minusDays(7);
                break;
            case "30days":
                startDate = endDate.minusDays(30);
                break;
            case "90days":
                startDate = endDate.minusDays(90);
                break;
            case "1year":
                startDate = endDate.minusYears(1);
                break;
            default:
                startDate = endDate.minusDays(30);
        }

        return new LocalDateTime[]{startDate, endDate};
    }

    private String getDayName(int index) {
        DayOfWeek[] days = DayOfWeek.values();
        return days[index % 7].name().substring(0, 3);
    }

    /* ----------------------- Public small helpers retained ----------------------- */

    public List<StatusDistribution> getStatusDistributionOnly(String companyPublicId,Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        Long companyId = resolveCompanyId(companyPublicId);
        return getStatusDistribution(companyId,departmentId, startDate, endDate);
    }

    public List<PriorityDistribution> getPriorityDistributionOnly(String companyPublicId,Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        Long companyId = resolveCompanyId(companyPublicId);
        return getPriorityDistribution(companyId,departmentId, startDate, endDate);
    }

    public KpiMetrics getKpiMetricsOnly(String companyPublicId,Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        Long companyId = resolveCompanyId(companyPublicId);
        return getKpiMetrics(companyId,departmentId, startDate, endDate);
    }

    public List<DepartmentPerformance> getDepartmentPerformanceOnly(String companyPublicId, LocalDateTime startDate, LocalDateTime endDate) {
        Long companyId = resolveCompanyId(companyPublicId);
        return getDepartmentPerformance(companyId, startDate, endDate);
    }

    public List<AssigneePerformance> getTopPerformers(String companyPublicId,Long departmentId, LocalDateTime startDate, LocalDateTime endDate, int limit) {
        Long companyId = resolveCompanyId(companyPublicId);
        return getAssigneePerformance(companyId, departmentId, startDate, endDate).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}



