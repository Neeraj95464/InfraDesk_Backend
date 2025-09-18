package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.TicketAssignmentRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface TicketAssignmentRuleRepository extends JpaRepository<TicketAssignmentRule, Long> {
    List<TicketAssignmentRule> findByCompanyIdOrderByPriorityDesc(Long companyId);

    List<TicketAssignmentRule> findByCompanyIdAndDepartmentIdAndLocationIdOrderByPriorityDesc(Long id, Long id1, Long id2);

    // Fetch all rules for a company ordered by priority descending
    List<TicketAssignmentRule> findByCompanyOrderByPriorityDesc(Company company);

    // Optional: fetch by company and location + department for future automation
    List<TicketAssignmentRule> findByCompanyAndLocationIdAndDepartmentIdOrderByPriorityDesc(
            Company company, Long locationId, Long departmentId);

    long countByCompany(Company company);

}
