package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.User;
import com.InfraDesk.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket>{

    Page<Ticket> findByCompany(Company company, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.company = :company AND t.status = :status")
    Page<Ticket> findByCompanyAndStatus(@Param("company") Company company,
                                        @Param("status") String status,
                                        Pageable pageable);

    Optional<Ticket> findByPublicId(String publicId);

    @Query("SELECT t FROM Ticket t WHERE t.company.id = :companyId "
            + "AND (:subject IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) "
            + "AND (:status IS NULL OR t.status = :status) "
            + "AND (:assigneeUserId IS NULL OR t.assignee.publicId = :assigneeUserId) "
    )
    Page<Ticket> searchTickets(@Param("companyId") Long companyId,
                               @Param("subject") String subject,
                               @Param("status") String status,
                               @Param("assigneeUserId") String assigneeUserId,
                               Pageable pageable);

    long countByAssignee_IdAndStatusIn(Long assigneeId, List<TicketStatus> statuses);

    long countByAssigneeAndStatusIn(User u, List<TicketStatus> open);



    Page<Ticket> findByCompany_PublicIdAndIsDeletedFalse(String companyId, Pageable pageable);

    Page<Ticket> findByCompany_PublicIdAndIsDeletedFalseAndSubjectContainingIgnoreCase(
            String companyId, String subject, Pageable pageable);

    Page<Ticket> findByCompany_PublicIdAndIsDeletedFalseAndDescriptionContainingIgnoreCase(
            String companyId, String description, Pageable pageable);

    Page<Ticket> findByCompany_PublicIdAndIsDeletedFalseAndPublicIdContainingIgnoreCase(
            String companyId, String publicId, Pageable pageable);

    Optional<Ticket> findByPublicIdAndCompany_PublicId(String ticketId, String companyId);

    Optional<Ticket> findByPublicIdAndCompanyId(String ticketPublicId, Long companyId);










    @Query("""
    SELECT COUNT(t)
    FROM Ticket t
    WHERE t.company.id = :companyId
      AND t.isDeleted = false
      AND (:departmentId IS NULL OR t.department.id = :departmentId)
""")
    Long countByCompanyAndOptionalDepartment(@Param("companyId") Long companyId,
                                             @Param("departmentId") Long departmentId);



    // Basic counts
        @Query("SELECT COUNT(t) FROM Ticket t WHERE t.company.id = :companyId AND t.isDeleted = false")
        Long countByCompanyId(@Param("companyId") Long companyId);

        @Query("SELECT COUNT(t) FROM Ticket t WHERE t.company.id = :companyId AND t.status = :status AND t.isDeleted = false")
        Long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") TicketStatus status);

    @Query("""
    SELECT COUNT(t)
    FROM Ticket t
    WHERE t.company.id = :companyId
      AND t.status = :status
      AND t.isDeleted = false
      AND (:departmentId IS NULL OR t.department.id = :departmentId)
""")
    Long countByCompanyAndStatusAndOptionalDepartment(@Param("companyId") Long companyId,
                                                      @Param("status") TicketStatus status,
                                                      @Param("departmentId") Long departmentId);


    // Status distribution
        @Query("SELECT t.status as status, COUNT(t) as count FROM Ticket t " +
                "WHERE t.company.id = :companyId AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY t.status")
        List<Map<String, Object>> getStatusDistribution(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // Priority distribution
        @Query("SELECT t.priority as priority, COUNT(t) as count FROM Ticket t " +
                "WHERE t.company.id = :companyId AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY t.priority")
        List<Map<String, Object>> getPriorityDistribution(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // Ticket trend by date
        @Query("SELECT DATE(t.createdAt) as date, COUNT(t) as count FROM Ticket t " +
                "WHERE t.company.id = :companyId AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY DATE(t.createdAt) ORDER BY DATE(t.createdAt)")
        List<Map<String, Object>> getTicketTrendByDate(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // Department performance
        @Query("SELECT d.name as department, COUNT(t) as tickets, " +
                "AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.updatedAt)) as avgTime " +
                "FROM Ticket t JOIN t.department d " +
                "WHERE t.company.id = :companyId AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY d.name")
        List<Map<String, Object>> getDepartmentPerformance(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // Ticket type distribution
        @Query("SELECT tt.name as type, COUNT(t) as count FROM Ticket t " +
                "JOIN t.ticketType tt " +
                "WHERE t.company.id = :companyId AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY tt.name ORDER BY COUNT(t) DESC")
        List<Map<String, Object>> getTicketTypeDistribution(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // Average resolution time by priority
        @Query("SELECT t.priority as priority, " +
                "AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.updatedAt)) as avgHours " +
                "FROM Ticket t " +
                "WHERE t.company.id = :companyId AND t.status = 'RESOLVED' AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY t.priority")
        List<Map<String, Object>> getAvgResolutionTimeByPriority(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // Assignee performance
        @Query("SELECT u.email as firstName, u.email as lastName, " +
                "COUNT(CASE WHEN t.status = 'RESOLVED' THEN 1 END) as resolved, " +
                "COUNT(CASE WHEN t.status != 'RESOLVED' AND t.status != 'CLOSED' THEN 1 END) as pending, " +
                "AVG(CASE WHEN t.status = 'RESOLVED' THEN TIMESTAMPDIFF(HOUR, t.createdAt, t.updatedAt) END) as avgTime " +
                "FROM Ticket t JOIN t.assignee u " +
                "WHERE t.company.id = :companyId AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY u.id, u.email, u.email " +
                "ORDER BY resolved DESC")
        List<Map<String, Object>> getAssigneePerformance(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // Hourly ticket volume
        @Query("SELECT HOUR(t.createdAt) as hour, COUNT(t) as count FROM Ticket t " +
                "WHERE t.company.id = :companyId AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY HOUR(t.createdAt) ORDER BY HOUR(t.createdAt)")
        List<Map<String, Object>> getHourlyTicketVolume(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // SLA compliance by department
        @Query("SELECT d.name as department, " +
                "COUNT(CASE WHEN t.updatedAt <= t.slaDueDate THEN 1 END) as onTime, " +
                "COUNT(CASE WHEN t.updatedAt > t.slaDueDate OR (t.slaDueDate < CURRENT_TIMESTAMP AND t.status != 'RESOLVED') THEN 1 END) as breached " +
                "FROM Ticket t JOIN t.department d " +
                "WHERE t.company.id = :companyId AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY d.name")
        List<Map<String, Object>> getSlaComplianceByDepartment(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // Ticket age distribution
        @Query("SELECT " +
                "CASE " +
                "  WHEN TIMESTAMPDIFF(HOUR, t.createdAt, CURRENT_TIMESTAMP) <= 24 THEN '0-24h' " +
                "  WHEN TIMESTAMPDIFF(HOUR, t.createdAt, CURRENT_TIMESTAMP) <= 72 THEN '1-3 days' " +
                "  WHEN TIMESTAMPDIFF(HOUR, t.createdAt, CURRENT_TIMESTAMP) <= 168 THEN '3-7 days' " +
                "  WHEN TIMESTAMPDIFF(HOUR, t.createdAt, CURRENT_TIMESTAMP) <= 336 THEN '7-14 days' " +
                "  WHEN TIMESTAMPDIFF(HOUR, t.createdAt, CURRENT_TIMESTAMP) <= 720 THEN '14-30 days' " +
                "  ELSE '30+ days' " +
                "END as age, COUNT(t) as count " +
                "FROM Ticket t " +
                "WHERE t.company.id = :companyId AND t.status IN ('OPEN', 'IN_PROGRESS', 'ON_HOLD') AND t.isDeleted = false " +
                "GROUP BY age")
        List<Map<String, Object>> getTicketAgeDistribution(@Param("companyId") Long companyId);

        // Location distribution
        @Query("SELECT l.name as location, COUNT(t) as tickets, " +
                "COUNT(CASE WHEN t.status = 'RESOLVED' THEN 1 END) as resolved, " +
                "COUNT(CASE WHEN t.status != 'RESOLVED' AND t.status != 'CLOSED' THEN 1 END) as pending " +
                "FROM Ticket t JOIN t.location l " +
                "WHERE t.company.id = :companyId AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY l.name ORDER BY tickets DESC")
        List<Map<String, Object>> getLocationDistribution(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // Average resolution time
        @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.updatedAt)) FROM Ticket t " +
                "WHERE t.company.id = :companyId AND t.status = 'RESOLVED' AND t.isDeleted = false " +
                "AND t.createdAt BETWEEN :startDate AND :endDate")
        Double getAverageResolutionTime(
                @Param("companyId") Long companyId,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate
        );

        // SLA compliance percentage
//        @Query("SELECT " +
//                "(COUNT(CASE WHEN t.updatedAt <= t.slaDueDate THEN 1 END) * 100.0 / COUNT(t)) " +
//                "FROM Ticket t " +
//                "WHERE t.company.id = :companyId AND t.status = 'RESOLVED' AND t.slaDueDate IS NOT NULL AND t.isDeleted = false " +
//                "AND t.createdAt BETWEEN :startDate AND :endDate")
//        Double getSlaCompliancePercentage(
//                @Param("companyId") Long companyId,
//                @Param("startDate") LocalDateTime startDate,
//                @Param("endDate") LocalDateTime endDate
//        );

    @Query("SELECT CASE WHEN COUNT(t) = 0 THEN 0 ELSE " +
            "(COUNT(CASE WHEN t.updatedAt <= t.slaDueDate THEN 1 END) * 100.0 / COUNT(t)) END " +
            "FROM Ticket t " +
            "WHERE t.company.id = :companyId AND t.status = 'RESOLVED' AND t.slaDueDate IS NOT NULL AND t.isDeleted = false " +
            "AND t.createdAt BETWEEN :startDate AND :endDate")
    Double getSlaCompliancePercentage(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );





//
//        /* ---------- Department-aware KPI Queries ---------- */
//        @Query("""
//        SELECT AVG(EXTRACT(EPOCH FROM (t.updatedAt - t.createdAt)) / 3600)
//        FROM Ticket t
//        WHERE t.company.id = :companyId
//          AND t.status = 'RESOLVED'
//          AND t.createdAt BETWEEN :start AND :end
//          AND t.isDeleted = false
//          AND (:departmentId IS NULL OR t.department.id = :departmentId)
//    """)
//        Double getAverageResolutionTime(@Param("companyId") Long companyId,
//                                        @Param("departmentId") Long departmentId,
//                                        @Param("start") LocalDateTime start,
//                                        @Param("end") LocalDateTime end);
//
//        @Query("""
//        SELECT ((COUNT(CASE WHEN t.updatedAt <= t.slaDueDate THEN 1 END) * 100.0)
//               / NULLIF(COUNT(t.id), 0))
//        FROM Ticket t
//        WHERE t.company.id = :companyId
//          AND t.status = 'RESOLVED'
//          AND t.slaDueDate IS NOT NULL
//          AND t.isDeleted = false
//          AND t.createdAt BETWEEN :start AND :end
//          AND (:departmentId IS NULL OR t.department.id = :departmentId)
//    """)
//        Double getSlaCompliancePercentage(@Param("companyId") Long companyId,
//                                          @Param("departmentId") Long departmentId,
//                                          @Param("start") LocalDateTime start,
//                                          @Param("end") LocalDateTime end);
//
//        /* ---------- Department-aware Distributions ---------- */
//
//        @Query("""
//        SELECT new map(t.status as status, COUNT(t) as count)
//        FROM Ticket t
//        WHERE t.company.id = :companyId
//          AND t.createdAt BETWEEN :start AND :end
//          AND t.isDeleted = false
//          AND (:departmentId IS NULL OR t.department.id = :departmentId)
//        GROUP BY t.status
//    """)
//        List<Map<String, Object>> getStatusDistribution(@Param("companyId") Long companyId,
//                                                        @Param("departmentId") Long departmentId,
//                                                        @Param("start") LocalDateTime start,
//                                                        @Param("end") LocalDateTime end);
//
//        @Query("""
//        SELECT new map(t.priority as priority, COUNT(t) as count)
//        FROM Ticket t
//        WHERE t.company.id = :companyId
//          AND t.createdAt BETWEEN :start AND :end
//          AND t.isDeleted = false
//          AND (:departmentId IS NULL OR t.department.id = :departmentId)
//        GROUP BY t.priority
//    """)
//        List<Map<String, Object>> getPriorityDistribution(@Param("companyId") Long companyId,
//                                                          @Param("departmentId") Long departmentId,
//                                                          @Param("start") LocalDateTime start,
//                                                          @Param("end") LocalDateTime end);
//
//        @Query("""
//        SELECT new map(t.ticketType.name as type, COUNT(t) as count)
//        FROM Ticket t
//        WHERE t.company.id = :companyId
//          AND t.createdAt BETWEEN :start AND :end
//          AND t.isDeleted = false
//          AND (:departmentId IS NULL OR t.department.id = :departmentId)
//        GROUP BY t.ticketType.name
//    """)
//        List<Map<String, Object>> getTicketTypeDistribution(@Param("companyId") Long companyId,
//                                                            @Param("departmentId") Long departmentId,
//                                                            @Param("start") LocalDateTime start,
//                                                            @Param("end") LocalDateTime end);
//
//        @Query("""
//        SELECT new map(t.priority as priority, AVG(EXTRACT(EPOCH FROM (t.updatedAt - t.createdAt)) / 3600) as avgHours)
//        FROM Ticket t
//        WHERE t.company.id = :companyId
//          AND t.status = 'RESOLVED'
//          AND t.createdAt BETWEEN :start AND :end
//          AND t.isDeleted = false
//          AND (:departmentId IS NULL OR t.department.id = :departmentId)
//        GROUP BY t.priority
//    """)
//        List<Map<String, Object>> getAvgResolutionTimeByPriority(@Param("companyId") Long companyId,
//                                                                 @Param("departmentId") Long departmentId,
//                                                                 @Param("start") LocalDateTime start,
//                                                                 @Param("end") LocalDateTime end);


    /* ---------- Department-aware KPI Queries ---------- */
//    @Query("""
//SELECT AVG(EXTRACT(EPOCH FROM (t.updatedAt - t.createdAt)) / 3600.0)
//FROM Ticket t
//WHERE t.company.id = :companyId
//  AND t.status = 'RESOLVED'
//  AND t.createdAt BETWEEN :start AND :end
//  AND t.isDeleted = false
//  AND (:departmentId IS NULL OR t.department.id = :departmentId)
//""")
//    Double getAverageResolutionTime(@Param("companyId") Long companyId,
//                                    @Param("departmentId") Long departmentId,
//                                    @Param("start") LocalDateTime start,
//                                    @Param("end") LocalDateTime end);

    @Query(value = """
        SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (t.updated_at - t.created_at)) / 3600.0), 0)
        FROM tickets t
        WHERE t.company_id = :companyId
          AND t.status = 'RESOLVED'
          AND t.created_at BETWEEN :start AND :end
          AND t.is_deleted = false
          AND (:departmentId IS NULL OR t.department_id = :departmentId)
        """, nativeQuery = true)
    Double getAverageResolutionTime(
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
SELECT (COALESCE(
    (COUNT(CASE WHEN t.updatedAt <= t.slaDueDate THEN 1 END) * 100.0)
    / NULLIF(COUNT(t.id), 0), 0.0)
)
FROM Ticket t
WHERE t.company.id = :companyId
  AND t.status = 'RESOLVED'
  AND t.slaDueDate IS NOT NULL
  AND t.isDeleted = false
  AND t.createdAt BETWEEN :start AND :end
  AND (:departmentId IS NULL OR t.department.id = :departmentId)
""")
    Double getSlaCompliancePercentage(@Param("companyId") Long companyId,
                                      @Param("departmentId") Long departmentId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    /* ---------- Department-aware Distributions ---------- */
    @Query("""
SELECT new map(t.status as status, COUNT(t) as count)
FROM Ticket t
WHERE t.company.id = :companyId
  AND t.createdAt BETWEEN :start AND :end
  AND t.isDeleted = false
  AND (:departmentId IS NULL OR t.department.id = :departmentId)
GROUP BY t.status
""")
    List<Map<String, Object>> getStatusDistribution(@Param("companyId") Long companyId,
                                                    @Param("departmentId") Long departmentId,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    @Query("""
SELECT new map(t.priority as priority, COUNT(t) as count)
FROM Ticket t
WHERE t.company.id = :companyId
  AND t.createdAt BETWEEN :start AND :end
  AND t.isDeleted = false
  AND (:departmentId IS NULL OR t.department.id = :departmentId)
GROUP BY t.priority
""")
    List<Map<String, Object>> getPriorityDistribution(@Param("companyId") Long companyId,
                                                      @Param("departmentId") Long departmentId,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    @Query("""
SELECT new map(t.ticketType.name as type, COUNT(t) as count)
FROM Ticket t
WHERE t.company.id = :companyId
  AND t.createdAt BETWEEN :start AND :end
  AND t.isDeleted = false
  AND (:departmentId IS NULL OR t.department.id = :departmentId)
GROUP BY t.ticketType.name
""")
    List<Map<String, Object>> getTicketTypeDistribution(@Param("companyId") Long companyId,
                                                        @Param("departmentId") Long departmentId,
                                                        @Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);


    @Query(value = """
    SELECT 
        t.priority AS priority,
        COALESCE(AVG(EXTRACT(EPOCH FROM (t.updated_at - t.created_at)) / 3600.0), 0) AS avg_hours
    FROM tickets t
    WHERE t.company_id = :companyId
      AND t.status = 'RESOLVED'
      AND t.created_at BETWEEN :start AND :end
      AND t.is_deleted = false
      AND (:departmentId IS NULL OR t.department_id = :departmentId)
    GROUP BY t.priority
    ORDER BY t.priority
""", nativeQuery = true)
    List<Map<String, Object>> getAvgResolutionTimeByPriority(
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}




