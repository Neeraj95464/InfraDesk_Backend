
package com.InfraDesk.service;

import com.InfraDesk.dto.SiteLocationAssignmentResponseDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Location;
import com.InfraDesk.entity.Site;
import com.InfraDesk.entity.SiteLocationAssignment;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.exception.NotFoundException;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.LocationRepository;
import com.InfraDesk.repository.SiteLocationAssignmentRepository;
import com.InfraDesk.repository.SiteRepository;
import com.InfraDesk.util.AuthUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteLocationAssignmentService {

    private final SiteRepository siteRepository;
    private final CompanyRepository companyRepository;
    private final LocationRepository locationRepository;
    private final SiteLocationAssignmentRepository assignmentRepository;
    private final AuthUtils authUtils;

    @Transactional
    public SiteLocationAssignmentResponseDTO linkLocationToSite(
            String companyPublicId,
            String locationPublicId,
            String targetSitePublicId
    ) {
        String currentUser = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"))
                .getEmail();

        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        Location location = locationRepository.findByPublicIdAndCompanyAndIsDeletedFalse(locationPublicId, company)
                .orElseThrow(() -> new NotFoundException("Location not found"));

        Site site = siteRepository.findByPublicIdAndCompanyAndIsDeletedFalse(targetSitePublicId, company)
                .orElseThrow(() -> new NotFoundException("Site not found"));

        if (location.getSite().getId().equals(site.getId())) {
            throw new BusinessException("This location is already linked with site as primary");
        }

        // Try to find a previously deleted assignment
        Optional<SiteLocationAssignment> found = assignmentRepository
                .findByCompanyAndLocationAndSiteAndIsDeletedTrue(company, location, site);

        SiteLocationAssignment assignment;
        if (found.isPresent()) {
            // Restore the assignment (undo soft delete)
            assignment = found.get();
            assignment.setIsDeleted(false);
            assignment.setIsActive(true);
            assignment.setUpdatedBy(currentUser);
            assignment.setUpdatedAt(LocalDateTime.now());
        } else {
            // Check for active assignment
            boolean exists = assignmentRepository
                    .existsByCompanyAndLocationAndSiteAndIsDeletedFalse(company, location, site);
            if (exists) {
                throw new IllegalArgumentException("Location already linked to this site");
            }

            // Create new assignment
            assignment = SiteLocationAssignment.builder()
                    .company(company)
                    .site(site)
                    .location(location)
                    .isActive(true)
                    .isDeleted(false)
                    .createdBy(currentUser)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        SiteLocationAssignment saved = assignmentRepository.save(assignment);

        return SiteLocationAssignmentResponseDTO.builder()
                .id(saved.getId())
                .companyPublicId(company.getPublicId())
                .sitePublicId(site.getPublicId())
                .siteName(site.getName())
                .locationPublicId(location.getPublicId())
                .locationName(location.getName())
                .isActive(saved.getIsActive())
                .createdBy(saved.getCreatedBy())
                .createdAt(saved.getCreatedAt())
                .build();
    }



    // New method to fetch all linked sites (including primary and assigned)
    public List<SiteLocationAssignmentResponseDTO> getAllLinkedSitesForLocation(String companyPublicId, String locationPublicId) {
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found"));

        Location location = locationRepository.findByPublicIdAndCompanyAndIsDeletedFalse(locationPublicId, company)
                .orElseThrow(() -> new NotFoundException("Location not found"));

        // Include primary site as well manually if needed
        Site primarySite = location.getSite();

        List<SiteLocationAssignment> assignments = assignmentRepository.findByLocationAndCompanyAndIsDeletedFalse(location, company);

        // Prepare DTO list including primary site and assigned linked sites
        List<SiteLocationAssignmentResponseDTO> linkedSites = assignments.stream().map(assignment ->
                SiteLocationAssignmentResponseDTO.builder()
                        .id(assignment.getId())
                        .companyPublicId(company.getPublicId())
                        .sitePublicId(assignment.getSite().getPublicId())
                        .siteName(assignment.getSite().getName())
                        .locationPublicId(location.getPublicId())
                        .locationName(location.getName())
                        .isActive(assignment.getIsActive())
                        .createdBy(assignment.getCreatedBy())
                        .createdAt(assignment.getCreatedAt())
                        .build()
        ).collect(Collectors.toList());

        // Optionally add primary site info as first element with id and created info as null or custom
        linkedSites.add(0, SiteLocationAssignmentResponseDTO.builder()
                .id(null)
                .companyPublicId(company.getPublicId())
                .sitePublicId(primarySite.getPublicId())
                .siteName(primarySite.getName())
                .locationPublicId(location.getPublicId())
                .locationName(location.getName())
                .isActive(null) // or true if preferred
                .createdBy(null)
                .createdAt(null)
                .build());

        return linkedSites;
    }
}
