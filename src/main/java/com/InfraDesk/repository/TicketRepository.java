package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.User;
import com.InfraDesk.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
}
