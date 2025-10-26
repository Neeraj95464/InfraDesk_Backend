package com.InfraDesk.service;

import com.InfraDesk.dto.EmployeeFilterRequest;
import com.InfraDesk.dto.EmployeeRequestDTO;
import com.InfraDesk.dto.EmployeeResponseDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.entity.*;
import com.InfraDesk.enums.Role;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.exception.NotFoundException;
import com.InfraDesk.mapper.EmployeeMapper;
import com.InfraDesk.repository.*;
import com.InfraDesk.specification.EmployeeSpecification;
import com.InfraDesk.util.AuthUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final TransactionTemplate transactionTemplate;
    private final Validator validator;

    public void createEmployeeWithUser(String companyId, EmployeeRequestDTO dto) {

        User authUser = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found with id: " + companyId));

        if (employeeRepository.findByEmployeeIdAndCompany_PublicId(dto.getEmployeeId(), companyId).isPresent()) {
            throw new BusinessException("Employee with ID " + dto.getEmployeeId() + " already exists in company " + companyId);
        }

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

//        employeeRepository.save(employee);
        try {
            saveEmployee(employee);
        } catch (Exception e) {
            log.error("Exception found while saving employee ", e);
            throw e; // 🔥 Re-throw so Spring can roll back properly
        }


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
            log.info("user membership exists already {} ",user.getEmail());
        }
    }

    public void saveEmployee(Employee employee) {
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        employeeRepository.save(employee);
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

    @Transactional
    public ImportResult importEmployeesFromExcel(InputStream is, String companyId) throws IOException {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int batchSize = 100; // Can adjust
        List<EmployeeRequestDTO> batch = new ArrayList<>();

        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new BusinessException("Company not found " + companyId));

        // Preload reference maps: name (lowercase trimmed) -> entity publicId for fast lookup
        Map<String, String> departmentNameToId = departmentRepository.findByCompany_PublicId(companyId).stream()
                .filter(d -> !d.getIsDeleted())
                .collect(Collectors.toMap(
                        d -> d.getName().toLowerCase().trim(),
                        Department::getPublicId
                ));

        Map<String, String> siteNameToId = siteRepository.findByCompany_PublicId(companyId).stream()
                .filter(s -> !s.getIsDeleted())
                .collect(Collectors.toMap(
                        s -> s.getName().toLowerCase().trim(),
                        Site::getPublicId
                ));

        Map<String, String> locationNameToId = locationRepository.findByCompany_PublicId(companyId).stream()
                .filter(l -> !l.getIsDeleted())
                .collect(Collectors.toMap(
                        l -> l.getName().toLowerCase().trim(),
                        Location::getPublicId
                ));

        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header row
                try {
                    EmployeeRequestDTO dto = new EmployeeRequestDTO();
                    dto.setEmployeeId(getCellValue(row.getCell(0)));
                    dto.setName(getCellValue(row.getCell(1)));
                    dto.setPhone(getCellValue(row.getCell(2)));

                    String deptName = getCellValue(row.getCell(3)).toLowerCase().trim();
                    String deptId = departmentNameToId.get(deptName);
                    if (deptId == null) throw new BusinessException("Department not found: " + deptName);
                    dto.setDepartmentId(deptId);

                    dto.setDesignation(getCellValue(row.getCell(4)));
                    dto.setEmail(getCellValue(row.getCell(5)));

                    String siteName = getCellValue(row.getCell(6)).toLowerCase().trim();
                    if (!siteName.isEmpty()) {
                        String siteId = siteNameToId.get(siteName);
                        if (siteId == null) throw new BusinessException("Site not found: " + siteName);
                        dto.setSiteId(siteId);
                    }

                    String locationName = getCellValue(row.getCell(7)).toLowerCase().trim();
                    if (!locationName.isEmpty()) {
                        String locationId = locationNameToId.get(locationName);
                        if (locationId == null) throw new BusinessException("Location not found: " + locationName);
                        dto.setLocationId(locationId);
                    }

                    dto.setPassword(getCellValue(row.getCell(8)));

                    String roleStr = getCellValue(row.getCell(9));
                    try {
                        dto.setRole(roleStr == null || roleStr.isBlank() ? Role.USER : Role.valueOf(roleStr.toUpperCase().trim()));
                    } catch (Exception ex) {
                        dto.setRole(Role.USER);
                    }

                    batch.add(dto);

                    if (batch.size() >= batchSize) {
                        successCount += processBatch(batch, companyId, errors);
                        batch.clear();
                    }
                } catch (Exception e) {
                    errors.add("Row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }

            if (!batch.isEmpty()) {
                successCount += processBatch(batch, companyId, errors);
            }
        }

        return new ImportResult(successCount, errors);
    }

    private int processBatch(List<EmployeeRequestDTO> batch, String companyId, List<String> errors) {
        int count = 0;
        for (int i = 0; i < batch.size(); i++) {
            EmployeeRequestDTO dto = batch.get(i);
//            try {
//                createEmployeeWithUser(companyId, dto);
//                count++;
//            } catch (Exception e) {
//                errors.add("Batch item #" + (i + 1) + ": " + e.getMessage());
//            }

            try {
                // each DTO handled independently — processBatch already called from your import loop
                transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                transactionTemplate.executeWithoutResult(status -> {
                    createEmployeeWithUser(companyId, dto);
                });
                count++;
            } catch (Exception e) {
                log.warn("Batch item #{} failed: {}", i + 1, e.getMessage(), e);
                errors.add("Batch item #" + (i + 1) + ": " + e.getMessage());
                // no global rollback — continue processing next item
            } finally {
                // reset propagation to default for safety
                transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            }
        }
        return count;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public static class ImportResult {
        private final int successCount;
        private final List<String> errors;

        public ImportResult(int successCount, List<String> errors) {
            this.successCount = successCount;
            this.errors = errors;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    public Page<EmployeeResponseDTO> filterEmployees(EmployeeFilterRequest req, String companyId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var spec = EmployeeSpecification.filter(req, companyId);

        Page<Employee> result = employeeRepository.findAll(spec, pageable);

        List<EmployeeResponseDTO> employees = result.getContent()
                .stream()
                .map(EmployeeMapper::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(employees, pageable, result.getTotalElements());
    }

}
