package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Department;
import com.InfraDesk.entity.TicketingDepartmentConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketingDepartmentConfigRepository extends JpaRepository<TicketingDepartmentConfig,Long> {
//    Page<TicketingDepartmentConfig> findByCompanyPublicId(String companyId, Pageable pageable);

//    Page<TicketingDepartmentConfig> searchByCompanyAndKeyword(String companyId, String likeSearch, Pageable pageable);

//    Optional<TicketingDepartmentConfig> findByPublicId(String publicId);

        Page<TicketingDepartmentConfig> findByCompanyPublicId(String companyPublicId, Pageable pageable);

        @Query("SELECT t FROM TicketingDepartmentConfig t WHERE lower(t.company.publicId) = lower(:companyId) AND " +
                "(lower(t.department.name) LIKE lower(concat('%', :keyword, '%')) OR lower(t.note) LIKE lower(concat('%', :keyword, '%')))")
        Page<TicketingDepartmentConfig> searchByCompanyAndKeyword(@Param("companyId") String companyId,
                                                                  @Param("keyword") String keyword,
                                                                  Pageable pageable);

        java.util.Optional<TicketingDepartmentConfig> findByPublicId(String publicId);

    Optional<TicketingDepartmentConfig> findByCompanyAndDepartment(Company company, Department department);

    // This ignores @Where, so will find even if isDeleted=true
    @Query("SELECT c FROM TicketingDepartmentConfig c WHERE c.company=:company AND c.department=:department")
    Optional<TicketingDepartmentConfig> findAnyByCompanyAndDepartment(@Param("company") Company company, @Param("department") Department department);

    @Query(
            value = "SELECT * FROM ticketing_department_config c WHERE c.company_id = :companyId AND c.department_id = :departmentId",
            nativeQuery = true
    )
    Optional<TicketingDepartmentConfig> findAnyIncludingDeleted(
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId);


    @Query("SELECT tdc FROM TicketingDepartmentConfig tdc JOIN FETCH tdc.department WHERE tdc.ticketEmail = :email AND tdc.isActive = true")
    Optional<TicketingDepartmentConfig> findWithDepartmentByTicketEmail(@Param("email") String email);

    Page<TicketingDepartmentConfig> findByCompanyAndNoteContainingIgnoreCaseAndIsDeletedFalse(Company company, String keyword, Pageable pageable);

    Page<TicketingDepartmentConfig> findByCompanyAndTicketEmailContainingIgnoreCaseAndIsDeletedFalse(Company company, String keyword, Pageable pageable);

    @Query("SELECT c FROM TicketingDepartmentConfig c WHERE c.company = :company AND (LOWER(c.note) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.ticketEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND c.isDeleted = false")
    Page<TicketingDepartmentConfig> searchByCompanyAndKeywordAndIsDeletedFalse(@Param("company") Company company, @Param("keyword") String keyword, Pageable pageable);



    Optional<TicketingDepartmentConfig> findByTicketEmailAndIsActiveTrue(String email);
}
