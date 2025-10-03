package com.InfraDesk.service;

import com.InfraDesk.dto.EmployeeRequestDTO;
import com.InfraDesk.dto.EmployeeResponseDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.entity.*;
import com.InfraDesk.enums.Role;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.exception.NotFoundException;
import com.InfraDesk.mapper.EmployeeMapper;
import com.InfraDesk.repository.*;
import com.InfraDesk.util.AuthUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final SiteRepository siteRepository;
    private final LocationRepository locationRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtils authUtils;
    private final CompanyDomainRepository companyDomainRepository;


    @Transactional
    public void createEmployeeWithUser(String companyId, EmployeeRequestDTO dto) {
        User authUser = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found with id: " + companyId));

        // Standardize and extract domain from email
        String email = dto.getEmail().toLowerCase();
        int atIdx = email.indexOf('@');
        if (atIdx < 0 || atIdx >= email.length() - 1) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        String emailDomain = email.substring(atIdx + 1).trim();

        isEmailDomainAllowed(company,emailDomain);

        // Find existing user or create new
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {

                    User newUser = User.builder()
                            .email(email)
                            .password(passwordEncoder.encode(dto.getPassword()))
                            .isActive(true)
                            .isDeleted(false)
                            .role(Role.USER)
                            .createdAt(LocalDateTime.now())
                            .createdBy(authUser.getEmployeeProfiles().get(0).getName())
                            .build();
                    return userRepository.save(newUser);
                });

        Department department = departmentRepository.findByPublicIdAndCompany_PublicId(dto.getDepartmentId(), companyId)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + dto.getDepartmentId()));

        Site site = null;
        if (dto.getSiteId() != null) {
            site = siteRepository.findByPublicIdAndCompany_PublicId(dto.getSiteId(), companyId).orElse(null);

        }

        Location location = null;
        if (dto.getLocationId() != null) {
            location = locationRepository.findByPublicIdAndCompany_PublicId(dto.getLocationId(), companyId).orElse(null);

        }

        Employee employee = Employee.builder()
                .employeeId(dto.getEmployeeId())
                .name(dto.getName())
                .phone(dto.getPhone())
                .designation(dto.getDesignation())
                .company(company)
                .department(department)
                .site(site)
                .location(location)
                .createdBy(authUser.getEmployeeProfiles().get(0).getName())
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .isActive(true)
                .user(user)
                .build();

        employeeRepository.save(employee);


        if (!membershipRepository.existsByUserAndCompany(user, company)) {
            Membership membership = Membership.builder()
                    .user(user)
                    .company(company)
                    .role(dto.getRole())
                    .isDeleted(false)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .createdBy(authUser.getEmployeeProfiles().getFirst().getName())
                    .build();
            membershipRepository.save(membership);

        } else {
            log.info("user membership exists already {} ",user);
        }
    }


    public void isEmailDomainAllowed(Company company, String emailDomain) {
        if (company == null || emailDomain == null || emailDomain.isBlank()) {
            throw new IllegalArgumentException("Company and email domain must be provided");
        }

        // Normalize domain case
        String normalizedEmailDomain = emailDomain.toLowerCase().trim();

        Set<String> allowedDomains = new HashSet<>();

        // Add primary domain of this company (lowercase)
        if (company.getDomain() != null) {
            allowedDomains.add(company.getDomain().toLowerCase());
        }

        List<CompanyDomain> validDomains;

        if (company.getParentCompany() != null && company.getParentCompany().getPublicId() != null) {
            // Fetch active, non-deleted domains of the parent company
            validDomains = companyDomainRepository
                    .findByCompany_PublicIdAndIsActiveTrueAndIsDeletedFalse(company.getParentCompany().getPublicId());
        } else {
            // Fetch active, non-deleted domains of this company
            validDomains = companyDomainRepository
                    .findByCompany_PublicIdAndIsActiveTrueAndIsDeletedFalse(company.getPublicId());
        }

        // Add all fetched domains from database
        for (CompanyDomain domain : validDomains) {
            if (domain.getDomain() != null) {
                allowedDomains.add(domain.getDomain().toLowerCase());
            }
        }

        // Check if email domain is allowed
        boolean domainAllowed = allowedDomains.contains(normalizedEmailDomain);
        if (!domainAllowed) {
            throw new BusinessException("ACCESS_DENIED",
                    "Email domain '" + normalizedEmailDomain + "' is not allowed for company " + company.getName());
        }

    }

    @Transactional
    public User createExternalUserWithMembership(String companyId, String email, String name) {
        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found with id: " + companyId));

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .password(passwordEncoder.encode("Welcome123"))                 // Password blank or random for guests
                            .role(Role.EXTERNAL_USER)      // A guest/limited role
                            .isActive(true)
                            .isDeleted(false)
                            .createdAt(LocalDateTime.now())
                            .createdBy("SYSTEM")           // Or other identifier
                            .build();
                    // Optionally store name if possible (add 'name' field to User)
                    return userRepository.save(newUser);
                });

        // Create membership if not already exists
        if (!membershipRepository.existsByUserAndCompany(user, company)) {
            Membership membership = Membership.builder()
                    .user(user)
                    .company(company)
                    .role(Role.EXTERNAL_USER)
                    .isDeleted(false)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .createdBy("SYSTEM")
                    .build();
            membershipRepository.save(membership);
        }
        return user;
    }




    public PaginatedResponse<EmployeeResponseDTO> getAllEmployees(String companyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employeePage = employeeRepository.findAllByCompany_PublicIdAndIsDeletedFalseAndIsActiveTrue(companyId,pageable);

        List<EmployeeResponseDTO> dtos = employeePage.getContent().stream()
                .map(EmployeeMapper::toDTO)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                dtos,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.isLast()
        );
    }


    public EmployeeResponseDTO getEmployeeByIdAndCompany(String companyPublicId, Long employeeId) {
        Employee employee = employeeRepository.findByIdAndCompany_PublicIdAndIsDeletedFalseAndIsActiveTrue(employeeId, companyPublicId)
                .orElseThrow(() -> new RuntimeException(
                        "Employee not found with id " + employeeId + " under company " + companyPublicId));

        return EmployeeMapper.toDTO(employee);
    }


    @Transactional
    public void updateEmployee(String companyId, String employeeId, EmployeeRequestDTO dto) {
        // Fetch employee ensuring it belongs to the given company and is active/not deleted
        Employee employee = employeeRepository.findByPublicIdAndCompany_PublicIdAndIsDeletedFalseAndIsActiveTrue(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException(
                        "Employee not found with id " + employeeId + " under company " + companyId));

        employee.setName(dto.getName());
        employee.setPhone(dto.getPhone());
        employee.setDesignation(dto.getDesignation());
        // If you have role field on employee, update it here

        // Update department if provided
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findByPublicIdAndCompany_PublicId(dto.getDepartmentId(),companyId)
                    .orElseThrow(() -> new RuntimeException("Department not found with id " + dto.getDepartmentId()));
            employee.setDepartment(department);
        }

        // Update site if provided; set to null if id is not valid or null
        if (dto.getSiteId() != null) {
            employee.setSite(siteRepository.
                    findByPublicIdAndCompany_PublicId(dto.getSiteId(),companyId)
                    .orElse(null));
        } else {
            employee.setSite(null);
        }

        // Update location if provided similarly
        if (dto.getLocationId() != null) {
            employee.setLocation(locationRepository
                    .findByPublicIdAndCompany_PublicId(dto.getLocationId(),companyId)
                    .orElse(null));
        } else {
            employee.setLocation(null);
        }

        // Save the updated employee entity
        employeeRepository.save(employee);
    }


    @Transactional
    public void deleteEmployee(String companyId, Long employeeId) {
        Employee employee = employeeRepository.findByIdAndCompany_PublicIdAndIsDeletedFalseAndIsActiveTrue(employeeId, companyId)
                .orElseThrow(() -> new RuntimeException(
                        "Employee not found with id " + employeeId + " under company " + companyId));

        // Soft delete flags
        employee.setIsActive(false);
        employee.setIsDeleted(true);

        employeeRepository.save(employee);
    }

}
