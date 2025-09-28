package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Department;
import com.InfraDesk.entity.TicketCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketCounterRepository extends JpaRepository<TicketCounter, Long> {
    // We will use EntityManager native locking in service. Provide a helper find for convenience.
    Optional<TicketCounter> findByCompanyIdAndDepartmentId(Long companyId, Long departmentId);
    Optional<TicketCounter> findByCompanyIdAndDepartmentIsNull(Long companyId);


        Optional<TicketCounter> findByCompanyAndDepartment(Company company, Department department);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tc FROM TicketCounter tc " +
            "WHERE tc.company = :company AND " +
            "(tc.department = :department OR (:department IS NULL AND tc.department IS NULL))")
    Optional<TicketCounter> findForUpdate(@Param("company") Company company,
                                          @Param("department") Department department);

}
