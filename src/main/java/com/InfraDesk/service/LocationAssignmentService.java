package com.InfraDesk.service;

import com.InfraDesk.entity.*;
import com.InfraDesk.exception.NotFoundException;
import com.InfraDesk.repository.*;
import com.InfraDesk.util.AuthUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocationAssignmentService {

    private final LocationRepository locationRepository;
    private final SiteRepository siteRepository;
    private final LocationAssignmentRepository locationAssignmentRepository;
    private final DepartmentRepository departmentRepository; // to fetch department if needed
    private final EmployeeRepository employeeRepository; // to fetch executive/manager if needed
    private final CompanyRepository companyRepository;
    private final AuthUtils authUtils; // get authenticated user

    @Transactional
    public LocationAssignment assignLocationToSite(
            String companyPublicId,
            String locationPublicId,
            String targetSitePublicId,
            Long departmentId,
            Long executiveId,
            Long managerId
    ) {
        User user = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        Location location = locationRepository.findByPublicIdAndCompanyAndIsDeletedFalse(locationPublicId, company)
                .orElseThrow(() -> new NotFoundException("Location not found"));

        Site targetSite = siteRepository.findByPublicIdAndCompanyAndIsDeletedFalse(targetSitePublicId, company)
                .orElseThrow(() -> new NotFoundException("Target Site not found"));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found"));

        Employee executive = employeeRepository.findById(executiveId)
                .orElseThrow(() -> new NotFoundException("Executive not found"));

        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException("Manager not found"));

        // Check if assignment already exists (optional)
        boolean exists = locationAssignmentRepository.existsByLocationAndSiteAndDepartmentAndIsDeletedFalse(location, targetSite, department);
        if (exists) {
            throw new IllegalArgumentException("Assignment already exists");
        }

        LocationAssignment assignment = LocationAssignment.builder()
                .location(location)
                .site(targetSite)
                .department(department)
                .executive(executive)
                .manager(manager)
                .isActive(true)
                .isDeleted(false)
                .createdBy(user.getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        return locationAssignmentRepository.save(assignment);
    }
}

