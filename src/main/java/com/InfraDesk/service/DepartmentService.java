//package com.InfraDesk.service;
//
//import com.InfraDesk.dto.CurrentUserDTO;
//import com.InfraDesk.entity.Company;
//import com.InfraDesk.entity.Department;
//import com.InfraDesk.entity.User;
//import com.InfraDesk.repository.CompanyRepository;
//import com.InfraDesk.repository.DepartmentRepository;
//import com.InfraDesk.util.AuthUtils;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class DepartmentService {
//
//    private final DepartmentRepository departmentRepository;
//    private final CompanyRepository companyRepository;
//    private final AuthUtils authUtils;
//
//    public static final List<String> DEFAULT_DEPARTMENTS =
//            List.of("IT", "HR", "Finance", "Operations");
//
//    /**
//     * Create a new Department under a given Company.
//     */
//    @Transactional
//    public Department createDepartment(Long companyId, Department department, String createdBy) {
//        Company company = validateCompanyAccess(companyId);
//
//        department.setCompany(company);
//        department.setCreatedBy(createdBy);
//        department.setIsDeleted(false);
//        department.setIsActive(true);
//
//        return departmentRepository.save(department);
//    }
//
//    /**
//     * Get all departments for a company (ignores soft-deleted).
//     */
//    @Transactional(readOnly = true)
//    public List<Department> getDepartmentsByCompany(Long companyId) {
//        validateCompanyAccess(companyId);
//        return departmentRepository.findByCompanyId(companyId);
//    }
//
//    /**
//     * Get department details but scoped to company.
//     */
//    @Transactional(readOnly = true)
//    public Department getDepartment(Long companyId, Long deptId) {
//        validateCompanyAccess(companyId);
//
//        return departmentRepository.findByIdAndCompanyId(deptId, companyId)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        "Department not found with ID: " + deptId + " for Company: " + companyId
//                ));
//    }
//
//    /**
//     * Add default departments when a company is created.
//     */
//    @Transactional
//    public List<Department> addDefaultDepartments(Long companyId) {
//        Company company = validateCompanyAccess(companyId);
//
//        return DEFAULT_DEPARTMENTS.stream()
//                .filter(depName -> !departmentRepository.existsByCompanyIdAndName(companyId, depName))
//                .map(depName -> {
//                    Department dep = Department.builder()
//                            .name(depName)
//                            .company(company)
//                            .createdBy("system") // or use authUtils.getCurrentUser()
//                            .isActive(true)
//                            .isDeleted(false)
//                            .build();
//
//                    return departmentRepository.save(dep);
//                })
//                .toList();
//    }
//
//
//    /**
//     * Update department safely under a company.
//     */
//    @Transactional
//    public Department updateDepartment(Long companyId, Long deptId, Department updatedDept) {
//        Department existing = getDepartment(companyId, deptId);
//
//        existing.setName(updatedDept.getName());
//        existing.setIsActive(updatedDept.getIsActive());
//
//        return departmentRepository.save(existing);
//    }
//
//    /**
//     * Soft delete a department under company.
//     */
//    @Transactional
//    public void deleteDepartment(Long companyId, Long deptId) {
//        Department existing = getDepartment(companyId, deptId);
//        departmentRepository.delete(existing); // triggers @SQLDelete
//    }
//
//    /**
//     * Ensures the authenticated user has access to this company (tenant isolation).
//     */
//    private Company validateCompanyAccess(Long companyId) {
////        Optional<CurrentUserDTO> authUser = authUtils.getCurrentUser(companyId); // e.g. from JWT
////        if (!authUser.equals(companyId)) {
////            throw new SecurityException("Unauthorized access to company " + companyId);
////        }
//
//        return companyRepository.findById(companyId)
//                .orElseThrow(() -> new EntityNotFoundException("Company not found with ID: " + companyId));
//    }
//}



package com.InfraDesk.service;

import com.InfraDesk.dto.DepartmentRequestDTO;
import com.InfraDesk.dto.DepartmentResponseDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Department;
import com.InfraDesk.entity.User;
import com.InfraDesk.exception.NotFoundException;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.DepartmentRepository;
import com.InfraDesk.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final AuthUtils authUtils;

    public Page<DepartmentResponseDTO> getDepartments(String companyPublicId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));

        return departmentRepository.findByCompanyAndIsDeletedFalse(company, pageable)
                .map(this::toResponseDTO);
    }

//    @Transactional
//    public DepartmentResponseDTO createDepartment(String companyPublicId, DepartmentRequestDTO dto) {
//        User user = authUtils.getAuthenticatedUser()
//                .orElseThrow(() -> new NotFoundException("Auth user not found"));
//
//        Company company = companyRepository.findByPublicId(companyPublicId)
//                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
//
//        // Optional: Check for existing department with same name in company
//        departmentRepository.findByNameAndCompanyAndIsDeletedFalse(dto.getName(), company)
//                .ifPresent(d -> {
//                    throw new IllegalArgumentException("Department name already exists in this company");
//                });
//
//        Department department = Department.builder()
//                .name(dto.getName())
//                .company(company)
//                .isActive(true)
//                .isDeleted(false)
//                .createdBy(user.getEmail())
//                .build();
//
//        return toResponseDTO(departmentRepository.save(department));
//    }

    public DepartmentResponseDTO createDepartment(String companyPublicId, DepartmentRequestDTO dto) {
        User user = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));

        if (departmentRepository.existsByNameAndCompanyAndIsDeletedFalse(dto.getName(), company)) {
            throw new IllegalArgumentException("Department name already exists in this company");
        }
//        Optional<Department> deletedDeptOpt = departmentRepository.findByNameAndCompanyAndIsDeletedTrue(dto.getName(), company);
        Optional<Department> deletedDeptOpt = departmentRepository.findDeletedByNameAndCompany(dto.getName(), company);


        if (deletedDeptOpt.isPresent()) {
            Department deletedDept = deletedDeptOpt.get();
            deletedDept.setIsDeleted(false);
            deletedDept.setIsActive(true);
            deletedDept.setUpdatedBy(user.getEmail());
            deletedDept.setUpdatedAt(LocalDateTime.now());
            return toResponseDTO(departmentRepository.save(deletedDept));
        }

        Department newDept = Department.builder()
                .name(dto.getName())
                .company(company)
                .isActive(true)
                .isDeleted(false)
                .createdBy(user.getEmail())
                .createdAt(LocalDateTime.now())
                .build();
        return toResponseDTO(departmentRepository.save(newDept));
    }


    @Transactional
    public DepartmentResponseDTO updateDepartment(String companyPublicId, Long deptId, DepartmentRequestDTO dto) {
        User user = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Auth user not found"));

        Department department = getDepartmentByIdAndCompany(deptId, companyPublicId);

        if (!department.getName().equals(dto.getName())) {
            // Optional uniqueness check on new name
            companyRepository.findByPublicId(companyPublicId)
                    .flatMap(company -> departmentRepository.findByNameAndCompanyAndIsDeletedFalse(dto.getName(), company))
                    .ifPresent(d -> {
                        throw new IllegalArgumentException("Department name already exists in this company");
                    });
        }

        department.setName(dto.getName());
        department.setIsActive(dto.getIsActive());
        department.setUpdatedBy(user.getEmail());
        // updatedAt is handled by @PreUpdate

        return toResponseDTO(departmentRepository.save(department));
    }

    @Transactional
    public void deleteDepartment(String companyPublicId, Long deptId) {
        User user = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        Department department = getDepartmentByIdAndCompany(deptId, companyPublicId);
        department.setIsDeleted(true);
        department.setIsActive(false);
        department.setUpdatedBy(user.getEmail());
        department.setUpdatedAt(LocalDateTime.now());
        departmentRepository.save(department);
    }


    private Department getDepartmentByIdAndCompany(Long deptId, String companyPublicId) {
        return departmentRepository.findById(deptId)
                .filter(dept -> !dept.getIsDeleted())
                .filter(dept -> dept.getCompany().getPublicId().equals(companyPublicId))
                .orElseThrow(() -> new NotFoundException("Department not found: " + deptId + " in company " + companyPublicId));
    }

    private DepartmentResponseDTO toResponseDTO(Department department) {
        return DepartmentResponseDTO.builder()
                .publicId(department.getPublicId())
                .name(department.getName())
                .companyId(department.getCompany().getId())
                .companyName(department.getCompany().getName())
                .isActive(department.getIsActive())
                .createdBy(department.getCreatedBy())
                .createdAt(department.getCreatedAt())
                .updatedBy(department.getUpdatedBy())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
