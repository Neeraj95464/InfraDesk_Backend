//package com.InfraDesk.repository;
//
//import com.InfraDesk.dto.DepartmentResponseDTO;
//import com.InfraDesk.entity.Company;
//import com.InfraDesk.entity.Department;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface DepartmentRepository extends JpaRepository<Department,Long> {
//    List<Department> findByCompanyId(Long companyId);
//    Optional<Department> findByIdAndCompanyId(Long id, Long companyId);
//
//    boolean existsByCompanyIdAndName(Long companyId, String depName);
//
//    List<DepartmentResponseDTO> findByCompanyAndIsDeletedFalse(Company company, Pageable pageable);
//}


package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Better to use Page for pagination rather than List
    Page<Department> findByCompanyAndIsDeletedFalse(Company company, Pageable pageable);

    Optional<Department> findByIdAndCompanyAndIsDeletedFalse(Long id, Company company);

    boolean existsByCompanyAndNameAndIsDeletedFalse(Company company, String depName);

    Optional<Department> findByNameAndCompanyAndIsDeletedTrue(String name, Company company);

    @Query("SELECT d FROM Department d WHERE d.name = :name AND d.company = :company AND d.isDeleted = true")
    Optional<Department> findDeletedByNameAndCompany(@Param("name") String name, @Param("company") Company company);
    
    Optional<Department> findByNameAndCompanyAndIsDeletedFalse(String name, Company company);

    boolean existsByNameAndCompanyAndIsDeletedFalse(String name, Company company);

    Optional<Department> findByPublicIdAndCompany_PublicId(String departmentPublicId, String companyPublicId);

    List<Department> findByCompany_PublicId(String companyId);

}
