package com.InfraDesk.service;

import com.InfraDesk.dto.MailIntegrationResponseDTO;
import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.MailIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

// MailIntegrationService.java
@Service
@RequiredArgsConstructor
public class MailIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(MailIntegrationService.class);
    private final CompanyRepository companyRepository;
    private final MailIntegrationRepository mailIntegrationRepository;

    public Page<MailIntegrationResponseDTO> getMailConfigs(String companyId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

//        Page<MailIntegration> pageEntities = mailIntegrationRepository.findByCompanyId(companyId, pageable);

        Page<MailIntegration> pageEntities = mailIntegrationRepository
                .findByCompanyIdAndIsDeletedFalse(companyId,pageable);

        return pageEntities.map(this::toDTO);
    }


//    public Page<MailIntegrationResponseDTO> getMailConfigs(String companyId, int page, int size) {
//
//        log.info("Request received in method ");
//        log.info("getMailConfigs called with companyId={}, page={}, size={}", companyId, page, size);
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        log.info("Pageable created: {}", pageable);
//
//        Page<MailIntegration> pageEntities = mailIntegrationRepository
//                .findByCompanyIdAndIsDeletedFalse(companyId, pageable);
//
//        log.info("Fetched Page: totalElements={}, totalPages={}, contentSize={}",
//                pageEntities.getTotalElements(), pageEntities.getTotalPages(), pageEntities.getContent().size());
//
//        Page<MailIntegrationResponseDTO> resultPage = pageEntities.map(this::toDTO);
//
//        log.info("Result DTO Page: totalElements={}, totalPages={}, contentSize={}",
//                resultPage.getTotalElements(), resultPage.getTotalPages(), resultPage.getContent().size());
//
//        return resultPage;
//    }


    public void updateMailIntegrationStatusOrDelete(String companyId, String publicId, String action) {
        MailIntegration mi = mailIntegrationRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("MailIntegration not found with publicId " + publicId));

        companyRepository.findByPublicId(companyId)
                .orElseThrow(() ->new BusinessException("Company not found "+companyId));

        switch (action.toLowerCase()) {
            case "activate":
                mi.setEnabled(true);
                mailIntegrationRepository.save(mi);
                break;
            case "deactivate":
                mi.setEnabled(false);
                mailIntegrationRepository.save(mi);
                break;
            case "delete":
                mi.setIsDeleted(true);
                mi.setEnabled(false);
                mailIntegrationRepository.save(mi);
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }
    }



    private MailIntegrationResponseDTO toDTO(MailIntegration entity) {
        return MailIntegrationResponseDTO.builder()
                .publicId(entity.getPublicId())
                .provider(entity.getProvider())
                .mailboxEmail(entity.getMailboxEmail())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                // map other fields you want to expose
                .build();
    }
}

