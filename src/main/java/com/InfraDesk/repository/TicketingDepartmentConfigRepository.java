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
}
