package com.InfraDesk.service;

import com.InfraDesk.dto.MailIntegrationResponseDTO;
import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.repository.MailIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

// MailIntegrationService.java
@Service
@RequiredArgsConstructor
public class MailIntegrationService {

    private final MailIntegrationRepository mailIntegrationRepository;

    public Page<MailIntegrationResponseDTO> getMailConfigs(String companyId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<MailIntegration> pageEntities = mailIntegrationRepository.findByCompanyId(companyId, pageable);

        return pageEntities.map(this::toDTO);
    }

    private MailIntegrationResponseDTO toDTO(MailIntegration entity) {
        return MailIntegrationResponseDTO.builder()
                .provider(entity.getProvider())
                .mailboxEmail(entity.getMailboxEmail())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                // map other fields you want to expose
                .build();
    }
}

