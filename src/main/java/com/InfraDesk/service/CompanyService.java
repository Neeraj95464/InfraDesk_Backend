package com.InfraDesk.service;

import com.InfraDesk.dto.CompanyDTO;
import com.InfraDesk.dto.CompanyDomainDTO;
import com.InfraDesk.dto.CompanyRegistrationRequest;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.entity.*;
import com.InfraDesk.enums.Role;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.exception.NotFoundException;
import com.InfraDesk.mapper.CompanyMapper;
import com.InfraDesk.repository.*;
import com.InfraDesk.util.AuthUtils;
import com.InfraDesk.util.DefaultRolePermissions;
import com.InfraDesk.util.ValidationUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyDomainRepository companyDomainRepository;
    private final AuthUtils authUtils;
    private final MembershipRepository membershipRepository;
    private final DepartmentRepository departmentRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final CompanyMapper companyMapper;

    @Transactional
    public CompanyDTO registerCompany(CompanyRegistrationRequest request) {

        // Normalize & validate inputs
        String email = request.getContactEmail().toLowerCase().trim();
        String emailDomain = email.substring(email.indexOf("@") + 1).toLowerCase();
        String requestDomain = request.getDomain().toLowerCase().trim();

        validateCompanyName(request.getCompanyName());

        if (ValidationUtils.isPublicEmailDomain(emailDomain)) {
            throw new BusinessException("Public domain email not allowed");
        }

        checkEmailNotInUse(email);

        // Collect domains
        Set<String> allDomains = new HashSet<>();
        allDomains.add(emailDomain);
        if (!emailDomain.equals(requestDomain)) {
            allDomains.add(requestDomain);
        }

        // load parentCompany by publicId if provided
        Company parentCompany = null;
         parentCompany = (request.getParentCompanyId() != null) ?
                companyRepository.findByPublicId(request.getParentCompanyId())
                        .orElseThrow(() -> new BusinessException("Parent Company not found")) : null;

        for (String domainStr : allDomains) {
            String normalized = domainStr.trim().toLowerCase();
            if (normalized.isEmpty()) continue;

            Optional<CompanyDomain> existingOpt = companyDomainRepository.findByDomainIgnoreCase(normalized);

            if (existingOpt.isPresent()) {
                CompanyDomain existingDomain = existingOpt.get();
                Company owningCompany = existingDomain.getCompany();

                boolean domainIsExtraDomainOfParent = parentCompany != null
                        && owningCompany.getPublicId().equals(parentCompany.getPublicId())
                        && !parentCompany.getDomain().equalsIgnoreCase(normalized);

                if (!domainIsExtraDomainOfParent) {
                    throw new BusinessException("DOMAIN_EXISTS", "Reserved Domain Found: " + normalized);
                }
            }
        }

        // Create company
        Company company = Company.builder()
                .name(request.getCompanyName())
                .legalName(request.getLegalName())
                .industry(request.getIndustry())
                .gstNumber(request.getGstNumber())
                .contactEmail(email)
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .logoUrl(request.getLogoUrl())
                .parentCompany(parentCompany)
                .domain(parentCompany != null ? requestDomain : emailDomain)
                .isActive(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        company = companyRepository.save(company);

        // Assign role
        Role assignedRole = (parentCompany == null) ? Role.PARENT_ADMIN : Role.COMPANY_ADMIN;

        // Create default admin (User + Employee + Department + Membership)
        createDefaultAdminUser(request, company, email, assignedRole);

        initializeCompanyPermissions(company);



        return companyMapper.toDto(company);
    }

    @Transactional
    public void initializeCompanyPermissions(Company company) {
        DefaultRolePermissions.MAP.forEach((role, perms) -> {
            perms.forEach(permissionCode -> {
                boolean exists = rolePermissionRepository.existsByCompanyAndRoleAndPermission(company, role, permissionCode);
                if (!exists) {
                    RolePermission rp = RolePermission.builder()
                            .company(company)
                            .role(role)
                            .permission(permissionCode)
                            .allowed(true)
                            .build();
                    rolePermissionRepository.save(rp);
                }
            });
        });
    }



    /**
     * Creates default admin user, IT department, employee record, and membership for a company.
     */
    private void createDefaultAdminUser(CompanyRegistrationRequest request, Company company, String email, Role role) {
        // 1. Create user
        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .role(role)
                .isActive(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build()
        );

        // 2. Create default IT department
        Department department = departmentRepository.save(Department.builder()
                .company(company)
                .name("IT")
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .isDeleted(false)
                .build()
        );

        // 3. Create employee record for admin
        Employee employee = employeeRepository.save(Employee.builder()
                .employeeId("PleaseChangeEmpId")
                .company(company)
                .name("PlsChangeYourName")
                .department(department)
                .phone("8888888888")
                .user(user)
                .isActive(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build()
        );

        // 4. Link membership
        membershipRepository.save(Membership.builder()
                .user(user)
                .company(company)
                .role(role)
                .isActive(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build()
        );

        // 2. Assign default role-permissions
        DefaultRolePermissions.MAP.forEach((defaultRole, codes) -> {
            codes.forEach(code -> {
                permissionRepository.findById(code).ifPresent(permission -> {
                    // check to avoid duplicate assignment
                    boolean exists = rolePermissionRepository
                            .existsByCompanyAndRoleAndPermission(company, role, permission.getCode());

                    if (!exists) {
                        RolePermission rp = RolePermission.builder()
                                .company(company)
                                .role(defaultRole)
                                .permission(permission.getCode())
                                .allowed(true)
                                .build();
                        rolePermissionRepository.save(rp);
                    }
                });

//                permissionRepository.findById(code).ifPresent(permission -> {
//                    boolean exists = rolePermissionRepository
//                            .existsByCompanyAndRoleAndPermission(company, role, permission); // pass enum, not entity
//
//                    if (!exists) {
//                        RolePermission rp = RolePermission.builder()
//                                .company(company)
//                                .role(role)
//                                .permission(permission.getCode()) // pass enum here as well
//                                .allowed(true)
//                                .build();
//                        rolePermissionRepository.save(rp);
//                    }
//                });

            });
        });


        log.info("Default admin user created for company {} with role {}", company.getId(), role);
    }

    private void validateDomainsNotInUse(Set<String> domains) {
        List<String> existingDomains = companyDomainRepository.findAllByDomainIn(domains)
                .stream()
                .map(CompanyDomain::getDomain)
                .toList();

        if (!existingDomains.isEmpty()) {
            throw new BusinessException("The following domains are already registered: " + existingDomains);
        }
    }

    private void checkEmailNotInUse(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("This email is already registered.");
        }
    }

    private void validateCompanyName(String companyName) {
        if (companyRepository.existsByName(companyName)) {
            throw new BusinessException("This company name is already registered.");
        }
    }

    @Transactional
    public List<CompanyDomain> addExtraDomains(List<String> extraDomains, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("Company not found."));

        if (extraDomains == null || extraDomains.isEmpty()) {
            throw new BusinessException("No domains provided.");
        }

        List<String> normalizedDomains = extraDomains.stream()
                .map(d -> d.toLowerCase().trim())
                .toList();

        // ✅ Find already registered domains in one query
        List<String> existing = companyDomainRepository.findAllByDomainIn((Set<String>) normalizedDomains)
                .stream()
                .map(CompanyDomain::getDomain)
                .toList();

        if (!existing.isEmpty()) {
            throw new BusinessException("The following domains are already registered: " + existing);
        }

        // ✅ Save new domains
        List<CompanyDomain> saved = new ArrayList<>();
        for (String domain : normalizedDomains) {
            CompanyDomain cd = CompanyDomain.builder()
                    .domain(domain)
                    .company(company)
                    .build();
            saved.add(companyDomainRepository.save(cd));
        }

        return saved;
    }


    public PaginatedResponse<CompanyDTO> getMyCompanies(int page, int size) {
        User currentUser = authUtils.getAuthenticatedUser()
                .orElseThrow(()->new NotFoundException("Authenticated user not found ")); // from SecurityContext
        Pageable pageable = PageRequest.of(page, size);

        Page<Membership> membershipsPage;

        // Super Admin → see all companies
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            Page<Company> companiesPage = companyRepository.findAll(pageable);
            return toPaginatedResponse(companiesPage);
        }

        // Otherwise → fetch by Memberships
        membershipsPage = membershipRepository.findByUserIdAndIsActiveTrue(
                currentUser.getId(), pageable
        );

        // Convert to Company Page
        List<Company> companies = membershipsPage
                .stream()
                .map(Membership::getCompany)
                .filter(Company::getIsActive)
                .toList();

        Page<Company> companiesPage = new PageImpl<>(companies, pageable, membershipsPage.getTotalElements());

        return toPaginatedResponse(companiesPage);
    }

    private PaginatedResponse<CompanyDTO> toPaginatedResponse(Page<Company> companiesPage) {
        List<CompanyDTO> dtos = companiesPage.getContent()
                .stream()
                .map(companyMapper::toDto)
                .toList();

        return new PaginatedResponse<>(
                dtos,
                companiesPage.getNumber(),
                companiesPage.getSize(),
                companiesPage.getTotalElements(),
                companiesPage.getTotalPages(),
                companiesPage.isLast()
        );
    }


    // Helper method to get active company
    public CompanyDTO getActiveCompanyById(Long id) {
        Company company = companyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Active company not found with ID: " + id));
        return companyMapper.toDto(company);
    }

    @Transactional
    public void updateCompany(Long id, Company updatedCompany) {
        Company existingCompany = companyRepository.findById(id)
                .orElseThrow(()->new BusinessException("Company not found with id "+id));

        existingCompany.setName(updatedCompany.getName());
        existingCompany.setLegalName(updatedCompany.getLegalName());
        existingCompany.setIndustry(updatedCompany.getIndustry());
        existingCompany.setGstNumber(updatedCompany.getGstNumber());
        existingCompany.setContactEmail(updatedCompany.getContactEmail());
        existingCompany.setContactPhone(updatedCompany.getContactPhone());
        existingCompany.setAddress(updatedCompany.getAddress());
        existingCompany.setLogoUrl(updatedCompany.getLogoUrl());
        existingCompany.setCurrentSubscription(updatedCompany.getCurrentSubscription());

        companyRepository.save(existingCompany);
    }


    @Transactional
    public void deleteCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        company.softDelete();
        companyRepository.save(company);
    }

    @Transactional
    public void deactivateCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        company.deactivate();
        companyRepository.save(company);
    }

    @Transactional
    public void activate(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        company.activate();
        companyRepository.save(company);
    }


}

