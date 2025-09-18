package com.InfraDesk.service;

import com.InfraDesk.dto.SiteRequestDTO;
import com.InfraDesk.dto.SiteResponseDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Site;
import com.InfraDesk.entity.User;
import com.InfraDesk.exception.NotFoundException;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.SiteRepository;
import com.InfraDesk.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final CompanyRepository companyRepository;
    private final AuthUtils authUtils;

    public Page<SiteResponseDTO> getSites(String companyPublicId, int page, int size) {
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
        Pageable pageable = PageRequest.of(page, size);
        return siteRepository.findByCompanyAndIsDeletedFalse(company, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    public SiteResponseDTO createSite(String companyPublicId, SiteRequestDTO dto) {
        User user = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));

        // Check if non-deleted site exists with same name
        boolean existsActive = siteRepository.existsByNameAndCompanyAndIsDeletedFalse(dto.getName(), company);
        if (existsActive) {
            throw new IllegalArgumentException("Site name already exists for this company");
        }

        // Check if a soft-deleted site with same name exists
        Optional<Site> deletedSiteOpt = siteRepository.findByNameAndCompanyAndIsDeletedTrue(dto.getName(), company);

        if (deletedSiteOpt.isPresent()) {
            Site deletedSite = deletedSiteOpt.get();
            // Restore soft-deleted site
            deletedSite.setIsDeleted(false);
            deletedSite.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
            deletedSite.setAddress(dto.getAddress());
            deletedSite.setUpdatedBy(user.getEmail());
            deletedSite.setUpdatedAt(LocalDateTime.now());
            Site restoredSite = siteRepository.save(deletedSite);
            return toResponseDTO(restoredSite);
        }

        // Create new site if none exists (active or soft deleted)
        Site site = Site.builder()
                .name(dto.getName())
                .company(company)
                .address(dto.getAddress())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .isDeleted(false)
                .createdBy(user.getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        Site savedSite = siteRepository.save(site);
        return toResponseDTO(savedSite);
    }


    @Transactional
    public SiteResponseDTO updateSite(String companyPublicId, String publicId, SiteRequestDTO dto) {
        User user = authUtils.getAuthenticatedUser()
                .orElseThrow(()-> new NotFoundException("Auth user not found "));

        Site site = getSiteByCompanyAndPublicId(companyPublicId, publicId);
        site.setName(dto.getName());
        site.setAddress(dto.getAddress());
        site.setIsActive(dto.getIsActive());
        site.setUpdatedBy(user.getEmail());
        // updatedAt handled automatically
        return toResponseDTO(siteRepository.save(site));
    }

    @Transactional
    public void deleteSite(String companyPublicId, String publicId) {
        User user = authUtils.getAuthenticatedUser()
                .orElseThrow(()-> new NotFoundException("Auth user not found "));

        Site site = getSiteByCompanyAndPublicId(companyPublicId, publicId);
        site.setIsDeleted(true);
        site.setIsActive(false);
        site.setUpdatedBy(user.getEmail());
        siteRepository.save(site);
    }

    private Site getSiteByCompanyAndPublicId(String companyPublicId, String publicId) {
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
        return siteRepository.findByPublicIdAndCompanyAndIsDeletedFalse(publicId, company)
                .orElseThrow(() -> new NotFoundException("Site not found"));
    }

    // Sample DTO mapping
    private SiteResponseDTO toResponseDTO(Site site) {
        return SiteResponseDTO.builder()
                .publicId(site.getPublicId())
                .name(site.getName())
                .address(site.getAddress())
                .isActive(site.getIsActive())
                .createdBy(site.getCreatedBy())
                .createdAt(site.getCreatedAt())
                .updatedBy(site.getUpdatedBy())
                .updatedAt(site.getUpdatedAt())
                .build();
    }
}

