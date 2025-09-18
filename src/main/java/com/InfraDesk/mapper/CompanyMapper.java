//package com.InfraDesk.mapper;
//
//import com.InfraDesk.dto.CompanyDTO;
//import com.InfraDesk.entity.Company;
//import com.InfraDesk.entity.CompanyDomain;
//import com.InfraDesk.repository.CompanyRepository;
//import lombok.RequiredArgsConstructor;
//
//import java.util.Collections;
//import java.util.List;
//
//@RequiredArgsConstructor
//public class CompanyMapper {
//    private final CompanyRepository companyRepository;
//
//    public static CompanyDTO toDto(Company company) {
//        if (company == null) return null;
//
//        // Defensive copy domains
////        List<String> domainNames = company.getDomains() == null
////                ? Collections.emptyList()
////                : company.getDomains().stream()
////                .map(CompanyDomain::getDomain)
////                .toList();
//
////        // Defensive copy subsidiaries: Don't recursively call parent!
////        List<CompanyDTO> childDtos = company.getSubsidiaries() == null
////                ? Collections.emptyList()
////                : company.getSubsidiaries().stream()
////                .map(child -> CompanyDTO.builder()
////                        .publicId(child.getPublicId())
////                        .name(child.getName())
////                        .legalName(child.getLegalName())
////                        .industry(child.getIndustry())
////                        .domain(child.getDomain())
////                        .isActive(child.getIsActive())
////                        .isDeleted(child.getIsDeleted())
////                        // ...add other fields as needed
////                        .build())
////                .toList();
//
//
//// In your CompanyMapper or service
//        public static List<CompanyDTO> mapSubsidiariesToDto(Company company, CompanyRepository companyRepository) {
//            // Defensive repository fetch -- ensures entities are fully loaded and detached from the original Hibernate set
//            List<Company> subsidiaries = company.getId() != null
//                    ? companyRepository.findByParentCompanyId(company.getId())
//                    : Collections.emptyList();
//
//            List<CompanyDTO> childDtos = subsidiaries.stream()
//                    .map(child -> CompanyDTO.builder()
//                            .publicId(child.getPublicId())
//                            .name(child.getName())
//                            .legalName(child.getLegalName())
//                            .industry(child.getIndustry())
//                            .domain(child.getDomain())
//                            .isActive(child.getIsActive())
//                            .isDeleted(child.getIsDeleted())
//                            // ...add fields as needed
//                            .build())
//                    .toList();
//
//            return childDtos;
//        }
//
//
//
//        return CompanyDTO.builder()
//                .publicId(company.getPublicId())
//                .name(company.getName())
//                .legalName(company.getLegalName())
//                // ... other flat fields ...
//                .domains(null)
//                .subsidiaries(childDtos) // mapped safely
//                .parentCompanyId(company.getParentCompany() != null
//                        ? company.getParentCompany().getId()
//                        : null)
//                .currentSubscriptionId(company.getCurrentSubscription() != null
//                        ? company.getCurrentSubscription().getId()
//                        : null)
//                .createdAt(company.getCreatedAt())
//                .createdBy(company.getCreatedBy())
//                .updatedAt(company.getUpdatedAt())
//                .updatedBy(company.getUpdatedBy())
//                .isActive(company.getIsActive())
//                .isDeleted(company.getIsDeleted())
//                .build();
//    }
//
//
//
//
//}
//
//
//
//



package com.InfraDesk.mapper;

import com.InfraDesk.dto.CompanyDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.CompanyDomain;
import com.InfraDesk.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompanyMapper {

    private final CompanyRepository companyRepository;

    /** Main method for mapping Company entity to CompanyDTO */
    public CompanyDTO toDto(Company company) {
        if (company == null) return null;

        // Defensive copy - domains
//        List<String> domainNames = company.getDomains() == null
//                ? Collections.emptyList()
//                : company.getDomains().stream()
//                .map(CompanyDomain::getDomain)
//                .collect(Collectors.toList());
//
//        // Defensive copy - subsidiaries, fetched from repo
        List<CompanyDTO> childDtos = mapSubsidiariesToDto(company);

        return CompanyDTO.builder()
                .publicId(company.getPublicId())
                .name(company.getName())
                .legalName(company.getLegalName())
                .industry(company.getIndustry())
                .gstNumber(company.getGstNumber())
                .contactEmail(company.getContactEmail())
                .contactPhone(company.getContactPhone())
                .address(company.getAddress())
                .logoUrl(company.getLogoUrl())
                .domain(company.getDomain())
                .domains(null)
                .subsidiaries(childDtos)
                .parentCompanyId(company.getParentCompany() != null
                        ? company.getParentCompany().getId()
                        : null)
                .currentSubscriptionId(company.getCurrentSubscription() != null
                        ? company.getCurrentSubscription().getId()
                        : null)
                .createdAt(company.getCreatedAt())
                .createdBy(company.getCreatedBy())
                .updatedAt(company.getUpdatedAt())
                .updatedBy(company.getUpdatedBy())
                .isActive(company.getIsActive())
                .isDeleted(company.getIsDeleted())
                .build();
    }

    /** Map subsidiaries to shallow (non-recursive) CompanyDTOs using repo, detached from Hibernate */
    public List<CompanyDTO> mapSubsidiariesToDto(Company company) {
        if (company.getId() == null) return Collections.emptyList();
        List<Company> subsidiaries = companyRepository.findByParentCompanyId(company.getId());
        return subsidiaries.stream()
                .map(child -> CompanyDTO.builder()
                        .publicId(child.getPublicId())
                        .name(child.getName())
                        .legalName(child.getLegalName())
                        .industry(child.getIndustry())
                        .domain(child.getDomain())
                        .contactEmail(child.getContactEmail())
                        .contactPhone(child.getContactPhone())
                        .address(child.getAddress())
                        .gstNumber(child.getGstNumber())
                        .logoUrl(child.getLogoUrl())
                        .isActive(child.getIsActive())
                        .isDeleted(child.getIsDeleted())
                        .createdAt(child.getCreatedAt())
                        .createdBy(child.getCreatedBy())
                        .parentCompanyId(child.getParentCompany().getId())
                        .subsidiaries(null)
                        .updatedAt(child.getUpdatedAt())
                        .updatedBy(child.getUpdatedBy())
                        .build())
                .collect(Collectors.toList());
    }
}
