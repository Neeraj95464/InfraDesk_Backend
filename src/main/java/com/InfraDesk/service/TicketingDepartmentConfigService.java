

package com.InfraDesk.service;

import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.TicketingDepartmentConfigCreateDTO;
import com.InfraDesk.dto.TicketingDepartmentConfigDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.CompanyDomain;
import com.InfraDesk.entity.Department;
import com.InfraDesk.entity.TicketingDepartmentConfig;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.mapper.TicketingDepartmentConfigMapper;
import com.InfraDesk.repository.CompanyDomainRepository;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.DepartmentRepository;
import com.InfraDesk.repository.TicketingDepartmentConfigRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketingDepartmentConfigService {

    private final TicketingDepartmentConfigRepository configRepository;

    private final CompanyRepository companyRepository;
    private final CompanyDomainRepository companyDomainRepository;

    private final DepartmentRepository departmentRepository;

    /**
     * Get paginated configs as DTO for company filtered and optional search
     */
    public PaginatedResponse<TicketingDepartmentConfigDTO> getConfigs(String companyId, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("department.name").ascending());

        Page<TicketingDepartmentConfig> result;
        if (search == null || search.isBlank()) {
            result = configRepository.findByCompanyPublicId(companyId, pageable);
        } else {
            String likeSearch = "%" + search.toLowerCase() + "%";
            result = configRepository.searchByCompanyAndKeyword(companyId, likeSearch, pageable);
        }

        // Map entity page to DTO page response
        return PaginatedResponse.of(
                result.map(TicketingDepartmentConfigMapper::toDto)
        );
    }

    /**
     * Get config DTO by publicId
     */
    public TicketingDepartmentConfigDTO getConfigByPublicId(String publicId) {
        TicketingDepartmentConfig entity = configRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Config not found: " + publicId));
        return TicketingDepartmentConfigMapper.toDto(entity);
    }

    public TicketingDepartmentConfigDTO createConfig(TicketingDepartmentConfigCreateDTO createDTO) {
        // Fetch company by publicId
        Company company = companyRepository.findByPublicId(createDTO.getCompanyPublicId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found with publicId: " + createDTO.getCompanyPublicId()));

        // Fetch department by publicId and company publicId
        Department department = departmentRepository.findByPublicIdAndCompany_PublicId(createDTO.getDepartmentPublicId(), company.getPublicId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found with publicId: " + createDTO.getDepartmentPublicId()));

        // Check for existing soft deleted config for same company + department
        Optional<TicketingDepartmentConfig> existingOpt = configRepository.findByCompanyAndDepartment(company, department);

        TicketingDepartmentConfig configEntity;

        if (existingOpt.isPresent()) {
            configEntity = existingOpt.get();
            if (Boolean.TRUE.equals(configEntity.getIsDeleted())) {
                // Restore soft-deleted config
                configEntity.setIsDeleted(false);
            }
        } else {
            configEntity = new TicketingDepartmentConfig();
            configEntity.setPublicId(UUID.randomUUID().toString());
            configEntity.setCompany(company);
            configEntity.setDepartment(department);
        }

        // Set core fields from DTO
        configEntity.setTicketEnabled(createDTO.getTicketEnabled() != null ? createDTO.getTicketEnabled() : Boolean.TRUE);
        configEntity.setTicketEmail(createDTO.getTicketEmail() != null ? createDTO.getTicketEmail().toLowerCase().trim() : null);
        configEntity.setNote(createDTO.getNote());
        configEntity.setIsActive(createDTO.getIsActive() != null ? createDTO.getIsActive() : Boolean.TRUE);
        configEntity.setIsDeleted(createDTO.getIsDeleted() != null ? createDTO.getIsDeleted() : Boolean.FALSE);

        // Set new domain control fields
        Boolean allowAny = createDTO.getAllowTicketsFromAnyDomain();
        if (allowAny == null) allowAny = Boolean.TRUE;
        configEntity.setAllowTicketsFromAnyDomain(allowAny);

        if (allowAny) {
            // Clear any allowed domains if open to any domain
            configEntity.getAllowedTicketDomains().clear();
        } else {
            Set<String> domains = createDTO.getAllowedDomainsForTicket() != null ?
                    createDTO.getAllowedDomainsForTicket()
                            .stream()
                            .map(String::toLowerCase)
                            .map(String::trim)
                            .filter(d -> !d.isEmpty())
                            .collect(Collectors.toSet())
                    : new HashSet<>();
            configEntity.getAllowedTicketDomains().clear();
            configEntity.getAllowedTicketDomains().addAll(domains);
        }

        // Validate ticketEmail domain if domain restriction enabled
        if (configEntity.getTicketEmail() != null && !allowAny) {
            int atIndex = configEntity.getTicketEmail().indexOf('@');
            if (atIndex < 0 || atIndex >= configEntity.getTicketEmail().length() - 1) {
                throw new IllegalArgumentException("Invalid ticketEmail format: " + configEntity.getTicketEmail());
            }
            String emailDomain = configEntity.getTicketEmail().substring(atIndex + 1);
            if (!configEntity.getAllowedTicketDomains().contains(emailDomain)) {
                throw new IllegalArgumentException("Email domain of ticketEmail is not allowed: " + emailDomain);
            }
        }

        // Save (new or updated)
        TicketingDepartmentConfig saved = configRepository.save(configEntity);
        return TicketingDepartmentConfigMapper.toDto(saved);
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

    /**
     * Update existing config by publicId using create DTO
     */
    public TicketingDepartmentConfigDTO updateConfig(String publicId, TicketingDepartmentConfigCreateDTO updateDTO) {
        TicketingDepartmentConfig existing = configRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Config not found: " + publicId));

        // Map from DTO to entity but update fields only (do not overwrite createdAt, etc.)
        existing.setTicketEnabled(updateDTO.getTicketEnabled());
        existing.setTicketEmail(updateDTO.getTicketEmail());
        existing.setNote(updateDTO.getNote());
        existing.setIsActive(updateDTO.getIsActive());
        existing.setIsDeleted(updateDTO.getIsDeleted());

        // Also update department and company references if needed
        if (updateDTO.getCompanyPublicId() != null) {
            Company company = companyRepository.findByPublicId(updateDTO.getCompanyPublicId())
                    .orElseThrow(() -> new EntityNotFoundException("Company not found"));
            existing.setCompany(company);

        }

        if (updateDTO.getDepartmentPublicId() != null) {
            Department department = departmentRepository
                    .findByPublicIdAndCompany_PublicId(updateDTO.getDepartmentPublicId(),updateDTO.getCompanyPublicId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found: " + updateDTO.getDepartmentPublicId()));
            existing.setDepartment(department);
        }

        TicketingDepartmentConfig saved = configRepository.save(existing);
        return TicketingDepartmentConfigMapper.toDto(saved);
    }

    /**
     * Soft delete by publicId
     */
    public void deleteConfig(String publicId) {
        TicketingDepartmentConfig existing = configRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Config not found: " + publicId));
        existing.setIsDeleted(true);
        configRepository.save(existing);
    }
}
