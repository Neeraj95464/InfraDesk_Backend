//package com.InfraDesk.service;
//
//import com.InfraDesk.entity.Company;
//import com.InfraDesk.entity.CompanyDomain;
//import com.InfraDesk.exception.BusinessException;
//import com.InfraDesk.repository.CompanyDomainRepository;
//import com.InfraDesk.repository.CompanyRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class CompanyDomainService {
//
//    private final CompanyDomainRepository companyDomainRepository;
//    private final CompanyRepository companyRepository;
//
//    // Create or add multiple new domains for a company
//    @Transactional
//    public List<CompanyDomain> addExtraDomains(List<String> domains, String companyId) {
//        if (domains == null || domains.isEmpty()) {
//            throw new BusinessException("Domain list cannot be empty");
//        }
//
//        Company company = companyRepository.findByPublicId(companyId)
//                .orElseThrow(() -> new BusinessException("Company not found with id: " + companyId));
//
//        for (String domain : domains) {
//            String normalized = domain.trim().toLowerCase();
//            if (normalized.isEmpty()) continue;
//
//            if (companyDomainRepository.existsByDomainAndIsActiveTrueAndIsDeletedFalse(normalized)) {
//                throw new BusinessException("Domain already reserved: " + normalized);
//            }
//        }
//
//        // Save all new domains
//        return domains.stream()
//                .map(d -> CompanyDomain.builder()
//                        .domain(d.trim().toLowerCase())
//                        .company(company)
//                        .isActive(true)
//                        .isDeleted(false)
//                        .build())
//                .map(companyDomainRepository::save)
//                .toList();
//    }
//
//    // Get paginated list of domains by company
//    public Page<CompanyDomain> getDomainsByCompany(String companyId, Pageable pageable) {
//        return companyDomainRepository.findByCompany_PublicIdAndIsDeletedFalse(companyId, pageable);
//    }
//
//    // Get domain by id and company
//    public CompanyDomain getDomainById(String companyId, String domainId) {
//        return companyDomainRepository.findByPublicIdAndCompany_PublicIdAndIsDeletedFalse(domainId, companyId)
//                .orElseThrow(() -> new BusinessException("Domain not found"));
//    }
//
//    // Update domain (only domain string and active flag)
//    @Transactional
//    public CompanyDomain updateDomain(String companyId, String domainId, String newDomain, Boolean isActive) {
//        CompanyDomain existing = getDomainById(companyId, domainId);
//
//        String normalized = newDomain.trim().toLowerCase();
//
//        if (!existing.getDomain().equalsIgnoreCase(normalized)) {
//            if (companyDomainRepository.existsByDomainAndIsActiveTrueAndIsDeletedFalse(normalized)) {
//                throw new BusinessException("Domain already exists: " + normalized);
//            }
//            existing.setDomain(normalized);
//        }
//
//        if (isActive != null) {
//            existing.setIsActive(isActive);
//        }
//
//        return companyDomainRepository.save(existing);
//    }
//
//    // Soft delete domain
//    @Transactional
//    public void deleteDomain(String companyId, String domainId) {
//        CompanyDomain domain = getDomainById(companyId, domainId);
//        domain.softDelete();
//        companyDomainRepository.save(domain);
//    }
//}



package com.InfraDesk.service;

import com.InfraDesk.dto.CompanyDomainDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.CompanyDomain;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.CompanyDomainRepository;
import com.InfraDesk.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyDomainService {

    private final CompanyDomainRepository companyDomainRepository;
    private final CompanyRepository companyRepository;

        @Transactional
        public List<CompanyDomainDTO> addExtraDomains(List<String> domains, String companyPublicId) {
            if (domains == null || domains.isEmpty()) {
                throw new BusinessException("Domain list cannot be empty");
            }

            Company company = companyRepository.findByPublicId(companyPublicId)
                    .orElseThrow(() -> new BusinessException("Company not found with id: " + companyPublicId));

            if (company.getParentCompany() != null) {
                throw new BusinessException("ACCESS_DENIED","Only parent company can reserve the domains");
            }

            List<CompanyDomainDTO> result = new ArrayList<>();

            for (String domainStr : domains) {
                String normalized = domainStr.trim().toLowerCase();
                if (normalized.isEmpty()) continue;

                Optional<CompanyDomain> existingOpt = companyDomainRepository.findByDomainIgnoreCase(normalized);
                if (existingOpt.isPresent()) {
                    throw new BusinessException("DOMAIN_EXISTS","Domain already reserved globally: " + normalized);
                }

                CompanyDomain newDomain = CompanyDomain.builder()
                        .domain(normalized)
                        .company(company)
                        .isActive(true)
                        .isDeleted(false)
                        .build();

                newDomain = companyDomainRepository.save(newDomain);

                result.add(new CompanyDomainDTO(newDomain.getPublicId(), newDomain.getDomain(), newDomain.getIsActive()));
            }

            if (result.isEmpty()) {
                throw new BusinessException("No valid new domains provided");
            }
            return result;
        }

    // Get paginated domains DTO by company public ID
    @Transactional(readOnly = true)
    public PaginatedResponse<CompanyDomainDTO> getDomainsByCompany(String companyPublicId, Pageable pageable) {
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new BusinessException("Company not found: " + companyPublicId));

        Page<CompanyDomain> page = companyDomainRepository.findByCompanyAndIsDeletedFalse(company, pageable);

        List<CompanyDomainDTO> dtoList = page.stream()
                .map(domain -> new CompanyDomainDTO(domain.getPublicId(), domain.getDomain(), domain.getIsActive()))
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                dtoList,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    // Get domain by publicIds
    @Transactional(readOnly = true)
    public CompanyDomainDTO getDomainById(String companyPublicId, String domainPublicId) {
        CompanyDomain domain = companyDomainRepository
                .findByPublicIdAndCompany_PublicIdAndIsDeletedFalse(domainPublicId, companyPublicId)
                .orElseThrow(() -> new BusinessException("Domain not found"));

        return new CompanyDomainDTO(domain.getPublicId(), domain.getDomain(), domain.getIsActive());
    }

    // Update domain
    @Transactional
    public CompanyDomainDTO updateDomain(String companyPublicId, String domainPublicId, String newDomain, Boolean isActive) {
        CompanyDomain existing = companyDomainRepository
                .findByPublicIdAndCompany_PublicIdAndIsDeletedFalse(domainPublicId, companyPublicId)
                .orElseThrow(() -> new BusinessException("Domain not found"));

        String normalized = newDomain.trim().toLowerCase();

        if (!existing.getDomain().equalsIgnoreCase(normalized)) {
            boolean exists = companyDomainRepository.existsByDomainAndIsActiveTrueAndIsDeletedFalseAndPublicIdNot(normalized, domainPublicId);
            if (exists) {
                throw new BusinessException("Domain already exists: " + normalized);
            }
            existing.setDomain(normalized);
        }

        if (isActive != null) {
            existing.setIsActive(isActive);
        }

        CompanyDomain updated = companyDomainRepository.save(existing);
        return new CompanyDomainDTO(updated.getPublicId(), updated.getDomain(), updated.getIsActive());
    }

    // Soft delete domain
    @Transactional
    public void deleteDomain(String companyPublicId, String domainPublicId) {
        CompanyDomain domain = companyDomainRepository
                .findByPublicIdAndCompany_PublicIdAndIsDeletedFalse(domainPublicId, companyPublicId)
                .orElseThrow(() -> new BusinessException("Domain not found"));

        domain.softDelete();
        companyDomainRepository.save(domain);
    }
}
