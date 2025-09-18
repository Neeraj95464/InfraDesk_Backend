package com.InfraDesk.repository;

import com.InfraDesk.entity.TicketCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketCounterRepository extends JpaRepository<TicketCounter, Long> {
    // We will use EntityManager native locking in service. Provide a helper find for convenience.
    Optional<TicketCounter> findByCompanyIdAndDepartmentId(Long companyId, Long departmentId);
    Optional<TicketCounter> findByCompanyIdAndDepartmentIsNull(Long companyId);
}
