package com.InfraDesk.service;

import com.InfraDesk.dto.CompanySettingsDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.CompanySettings;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.CompanySettingsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanySettingsService {

    private final CompanySettingsRepository settingsRepository;
    private final CompanyRepository companyRepository;

    // Save or update settings for a company
    @Transactional
    public CompanySettingsDTO saveSettings(String companyId, CompanySettingsDTO dto) {
        Company company = companyRepository.findByPublicIdAndIsDeletedFalse(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        CompanySettings settings = settingsRepository.findByCompanyIdAndIsDeletedFalse(companyId)
                .orElse(CompanySettings.builder().companyId(companyId).build());


        settings.setTicketDefaultDueDays(dto.getTicketDefaultDueDays());
        settings.setCompanyShortCode(dto.getCompanyShortCode());
        settings.setAssetTagRequired(dto.getAssetTagRequired());
        settings.setCreatedAt(settings.getCreatedAt() != null ? settings.getCreatedAt() : java.time.LocalDateTime.now());
        settings.setIsDeleted(false);

        CompanySettings saved = settingsRepository.save(settings);
        company.setShortCode(dto.getCompanyShortCode());
        companyRepository.save(company);
        return toDTO(saved);
    }

    // Retrieve company settings, fallback to parent recursively
    public CompanySettingsDTO getSettings(String companyId) {
        Company company = companyRepository.findByPublicIdAndIsDeletedFalse(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return getSettingsRecursive(company);
    }

    private CompanySettingsDTO getSettingsRecursive(Company company) {
        return settingsRepository.findByCompanyIdAndIsDeletedFalse(company.getPublicId())
                .map(this::toDTO)
                .or(() -> company.getParentCompany() != null ?
                        Optional.ofNullable(getSettingsRecursive(company.getParentCompany())) :
                        Optional.empty())
                .orElseThrow(() -> new RuntimeException("Settings not found for company and parent hierarchy"));
    }

    public CompanySettingsDTO toDTO(CompanySettings settings) {
        return CompanySettingsDTO.builder()
                .ticketDefaultDueDays(settings.getTicketDefaultDueDays())
                .companyShortCode(settings.getCompanyShortCode())
                .assetTagRequired(settings.getAssetTagRequired())
                .build();
    }
}

