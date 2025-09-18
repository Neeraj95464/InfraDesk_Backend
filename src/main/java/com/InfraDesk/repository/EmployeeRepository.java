package com.InfraDesk.repository;

import com.InfraDesk.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findByUserIdAndCompanyId(Long userId, Long companyId);

    List<Employee> findByUserId(Long userId);

    Page<Employee> findAllByCompany_PublicIdAndIsDeletedFalseAndIsActiveTrue(String companyPublicId, Pageable pageable);

    Optional<Employee> findByIdAndCompany_PublicIdAndIsDeletedFalseAndIsActiveTrue(Long id, String companyPublicId);

    Optional<Employee> findByPublicIdAndCompany_PublicIdAndIsDeletedFalseAndIsActiveTrue(String employeeId, String companyId);

}
