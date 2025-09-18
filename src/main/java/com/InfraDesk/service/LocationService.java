package com.InfraDesk.service;

import com.InfraDesk.dto.LocationRequestDTO;
import com.InfraDesk.dto.LocationResponseDTO;
import com.InfraDesk.dto.LocationWithSitesDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Location;
import com.InfraDesk.entity.Site;
import com.InfraDesk.entity.SiteLocationAssignment;
import com.InfraDesk.exception.NotFoundException;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.LocationRepository;
import com.InfraDesk.repository.SiteLocationAssignmentRepository;
import com.InfraDesk.repository.SiteRepository;
import com.InfraDesk.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final SiteRepository siteRepository;
    private final CompanyRepository companyRepository;
    private final AuthUtils authUtils;  // For getting authenticated user email
    private final SiteLocationAssignmentRepository siteLocationAssignmentRepository;

//    public Page<LocationResponseDTO> getLocations(String companyPublicId, String sitePublicId, int page, int size) {
//        Company company = companyRepository.findByPublicId(companyPublicId)
//                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
//        Site site = siteRepository.findByPublicIdAndCompanyAndIsDeletedFalse(sitePublicId, company)
//                .orElseThrow(() -> new NotFoundException("Site not found: " + sitePublicId));
//
//        Pageable pageable = PageRequest.of(page, size);
//        return locationRepository.findBySiteAndCompanyAndIsDeletedFalse(site, company, pageable)
//                .map(this::toResponseDTO);
//    }

//    public Page<LocationResponseDTO> getLocationsIncludingAssigned(String companyPublicId, String sitePublicId, int page, int size) {
//        Company company = companyRepository.findByPublicId(companyPublicId)
//                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
//        Site site = siteRepository.findByPublicIdAndCompanyAndIsDeletedFalse(sitePublicId, company)
//                .orElseThrow(() -> new NotFoundException("Site not found: " + sitePublicId));
//
//        Pageable pageable = PageRequest.of(page, size);
//
//        // 1. Fetch primary locations of site (exact site as primary)
//        Page<Location> primaryLocations = locationRepository.findBySiteAndCompanyAndIsDeletedFalse(site, company, pageable);
//
//        // 2. Fetch assigned locations linked to this site via SiteLocationAssignment
//        List<Location> assignedLocations = siteLocationAssignmentRepository
//                .findAllBySiteAndCompanyAndIsDeletedFalse(site, company)
//                .stream()
//                .map(SiteLocationAssignment::getLocation)
//                .filter(loc -> loc.getIsActive() && !loc.getIsDeleted())
//                .distinct()
//                .collect(Collectors.toList());
//
//        // 3. Combine results uniquely
//        List<Location> combinedLocations = new ArrayList<>(primaryLocations.getContent());
//        for (Location loc : assignedLocations) {
//            if (!combinedLocations.contains(loc)) {
//                combinedLocations.add(loc);
//            }
//        }
//
//        // 4. Manual pagination of combined result
//        int start = (int) pageable.getOffset();
//        int end = Math.min((start + pageable.getPageSize()), combinedLocations.size());
//        List<Location> pagedList = combinedLocations.subList(start, end);
//
//        return new PageImpl<>(pagedList.stream().map(this::toResponseDTO).collect(Collectors.toList()), pageable, combinedLocations.size());
//    }

    public Page<LocationResponseDTO> getLocationsIncludingAssigned(
            String companyPublicId, String sitePublicId, int page, int size
    ) {
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
        Site site = siteRepository.findByPublicIdAndCompanyAndIsDeletedFalse(sitePublicId, company)
                .orElseThrow(() -> new NotFoundException("Site not found: " + sitePublicId));

        Pageable pageable = PageRequest.of(page, size);
//        System.out.println("DEBUG: company=" + company.getPublicId() + ", site=" + site.getPublicId());

        // Defensive: Fetch ALL primary locations for the site/company (unpaged for combining, paged for performance if very large)
        List<Location> primaryLocations = new ArrayList<>(locationRepository
                .findBySiteAndCompanyAndIsDeletedFalse(site, company, Pageable.unpaged()).getContent());
//        System.out.println("DEBUG: primaryLocations.size()=" + primaryLocations.size());

        // Defensive: Fetch ALL assigned locations through assignments (distinct by publicId)
        List<Location> assignedLocations = siteLocationAssignmentRepository
                .findAllBySiteAndCompanyAndIsDeletedFalse(site, company)
                .stream()
                .map(SiteLocationAssignment::getLocation)
                .filter(loc -> loc != null && Boolean.TRUE.equals(loc.getIsActive()) && Boolean.FALSE.equals(loc.getIsDeleted()))
                .collect(Collectors.toList());
//        System.out.println("DEBUG: assignedLocations.size()=" + assignedLocations.size());

        // Defensive: merge with LinkedHashMap to guarantee uniqueness by publicId and preserve primary order
        Map<String, Location> uniqueLocationMap = new LinkedHashMap<>();
        for (Location loc : primaryLocations) {
            if (loc != null) uniqueLocationMap.put(loc.getPublicId(), loc);
        }
        for (Location loc : assignedLocations) {
            if (loc != null) uniqueLocationMap.putIfAbsent(loc.getPublicId(), loc); // add only if absent
        }
//        System.out.println("DEBUG: uniqueLocationMap.size()=" + uniqueLocationMap.size());

        List<Location> combinedLocations = new ArrayList<>(uniqueLocationMap.values());

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), combinedLocations.size());
//        System.out.println("DEBUG: pagination range [" + start + "," + end + ")");

        List<LocationResponseDTO> pageContent;
        if (start < end) {
            pageContent = combinedLocations.subList(start, end).stream()
                    .map(loc -> {
                        try {
                            return toResponseDTO(loc);
                        } catch (Exception e) {
//                            System.out.println("DEBUG: Error mapping Location to DTO: " + e.getMessage());
                            throw e;
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            pageContent = Collections.emptyList();
        }

//        System.out.println("DEBUG: DTOs returned=" + pageContent.size());
        return new PageImpl<>(pageContent, pageable, combinedLocations.size());
    }

    private LocationResponseDTO toResponseDTO(Location location) {
        String sitePublicId = (location.getSite() != null) ? location.getSite().getPublicId() : null;
        String siteName = (location.getSite() != null) ? location.getSite().getName() : null;

        return LocationResponseDTO.builder()
                .publicId(location.getPublicId())
                .name(location.getName())
                .description(location.getDescription())
                .sitePublicId(sitePublicId)
                .siteName(siteName)
                .isActive(location.getIsActive())
                .createdBy(location.getCreatedBy())
                .createdAt(location.getCreatedAt())
                .updatedBy(location.getUpdatedBy())
                .updatedAt(location.getUpdatedAt())
                .build();
    }



    // Your existing Location entity to DTO mapper
//    private LocationResponseDTO toResponseDTO(Location location) {
//        // Map Location entity fields to LocationResponseDTO appropriately
//        return LocationResponseDTO.builder()
//                .publicId(location.getPublicId())
//                .name(location.getName())
//                .address(location.getDescription())
//                .isActive(location.getIsActive())
//                .createdBy(location.getCreatedBy())
//                .createdAt(location.getCreatedAt())
//                .updatedBy(location.getUpdatedBy())
//                .updatedAt(location.getUpdatedAt())
//                .build();
//    }


    @Transactional(readOnly = true)
    public Page<LocationResponseDTO> getLocationsForCompany(String companyPublicId, int page, int size, String search) {
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));

        Pageable pageable = PageRequest.of(page, size);

        // Assuming you add a repository method that fetches all locations for a company (across all sites)
        Page<Location> locationsPage;

        if (search == null || search.trim().isEmpty()) {
            locationsPage = locationRepository.findByCompanyAndIsDeletedFalse(company, pageable);
        } else {
            locationsPage = locationRepository.findByCompanyAndIsDeletedFalseAndNameContainingIgnoreCase(company, search.trim(), pageable);
        }

        return locationsPage.map(this::toResponseDTO);
    }

//    @Transactional(readOnly = true)
//    public Page<LocationResponseDTO> getMultiSiteLinkedLocations(String companyPublicId, int page, int size, String search) {
//        Company company = companyRepository.findByPublicId(companyPublicId)
//                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
//
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<Location> locationsPage;
//
//        if (search == null || search.trim().isEmpty()) {
//            // You need to implement this repository method to fetch locations linked to multiple sites
//            locationsPage = locationRepository.findMultiSiteLinkedLocationsByCompany(company, pageable);
//        } else {
//            locationsPage = locationRepository.findMultiSiteLinkedLocationsByCompanyAndNameContainingIgnoreCase(company, search.trim(), pageable);
//        }
//
//        return locationsPage.map(this::toResponseDTO);
//    }


//    @Transactional(readOnly = true)
//    public Page<LocationWithSitesDTO> getMultiSiteLinkedLocations(String companyPublicId, int page, int size, String search) {
//        Company company = companyRepository.findByPublicId(companyPublicId)
//                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
//
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Location> locationsPage;
//
//        if (search == null || search.trim().isEmpty()) {
//            locationsPage = locationRepository.findMultiSiteLinkedLocationsByCompany(company, pageable);
//        } else {
//            locationsPage = locationRepository.findMultiSiteLinkedLocationsByCompanyAndNameContainingIgnoreCase(company, search.trim(), pageable);
//        }
//
//        return locationsPage.map(this::toLocationWithSitesDTO);
//    }

//    @Transactional(readOnly = true)
//    public Page<LocationWithSitesDTO> getMultiSiteLinkedLocations(String companyPublicId, int page, int size, String search) {
//        Company company = companyRepository.findByPublicId(companyPublicId)
//                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
//
//        System.out.println("Request received to send multi site locations ");
//
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Location> locationsPage;
//
//        if (search == null || search.trim().isEmpty()) {
//            locationsPage = locationRepository.findMultiSiteLinkedLocationsByCompany(company, pageable);
//        } else {
//            locationsPage = locationRepository.findMultiSiteLinkedLocationsByCompanyAndNameContainingIgnoreCase(company, search.trim(), pageable);
//        }
//
//        return locationsPage.map(location -> {
//            // Collect assigned sites that are active and not deleted
//            List<LocationWithSitesDTO.SiteInfo> linkedSites = location.getAssignedSites().stream()
//                    .filter(assignment -> Boolean.TRUE.equals(assignment.getIsActive()) && Boolean.FALSE.equals(assignment.getIsDeleted()))
//                    .map(assignment -> LocationWithSitesDTO.SiteInfo.builder()
//                            .sitePublicId(assignment.getSite().getPublicId())
//                            .siteName(assignment.getSite().getName())
//                            .build())
//                    .toList();
//
//            // Optionally include primary site in linkedSites if not already present
//            String primarySitePublicId = location.getSite().getPublicId();
//            boolean primaryIncluded = linkedSites.stream()
//                    .anyMatch(s -> s.getSitePublicId().equals(primarySitePublicId));
//            if (!primaryIncluded) {
//                linkedSites.add(LocationWithSitesDTO.SiteInfo.builder()
//                        .sitePublicId(primarySitePublicId)
//                        .siteName(location.getSite().getName())
//                        .build());
//            }
//
//            return LocationWithSitesDTO.builder()
//                    .publicId(location.getPublicId())
//                    .name(location.getName())
//                    .description(location.getDescription())
//                    .isActive(location.getIsActive())
//                    .createdBy(location.getCreatedBy())
//                    .createdAt(location.getCreatedAt())
//                    .updatedBy(location.getUpdatedBy())
//                    .updatedAt(location.getUpdatedAt())
//                    .primarySitePublicId(primarySitePublicId)
//                    .primarySiteName(location.getSite().getName())
//                    .linkedSites(linkedSites)
//                    .build();
//        });
//    }


    @Transactional(readOnly = true)
    public Page<LocationWithSitesDTO> getMultiSiteLinkedLocations(
            String companyPublicId, int page, int size, String search) {

        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));

//        System.out.println("Request received to send multi site locations ");

        Pageable pageable = PageRequest.of(page, size);
        Page<Location> locationsPage;

        if (search == null || search.trim().isEmpty()) {
            locationsPage = locationRepository.findMultiSiteLinkedLocationsByCompany(company, pageable);
        } else {
            locationsPage = locationRepository.findMultiSiteLinkedLocationsByCompanyAndNameContainingIgnoreCase(
                    company, search.trim(), pageable);
        }

        return locationsPage.map(location -> {
            // Make mutable for adding primary site
            List<LocationWithSitesDTO.SiteInfo> linkedSites = location.getAssignedSites().stream()
                    .filter(assignment -> Boolean.TRUE.equals(assignment.getIsActive())
                            && Boolean.FALSE.equals(assignment.getIsDeleted()))
                    .map(assignment -> LocationWithSitesDTO.SiteInfo.builder()
                            .sitePublicId(assignment.getSite().getPublicId())
                            .siteName(assignment.getSite().getName())
                            .build())
                    .collect(Collectors.toCollection(ArrayList::new)); // ensures mutability

            // Primary site info
            String primarySitePublicId = location.getSite().getPublicId();
            boolean primaryIncluded = linkedSites.stream()
                    .anyMatch(s -> s.getSitePublicId().equals(primarySitePublicId));
            if (!primaryIncluded) {
                linkedSites.add(LocationWithSitesDTO.SiteInfo.builder()
                        .sitePublicId(primarySitePublicId)
                        .siteName(location.getSite().getName())
                        .build());
            }

            return LocationWithSitesDTO.builder()
                    .publicId(location.getPublicId())
                    .name(location.getName())
                    .description(location.getDescription())
                    .isActive(location.getIsActive())
                    .createdBy(location.getCreatedBy())
                    .createdAt(location.getCreatedAt())
                    .updatedBy(location.getUpdatedBy())
                    .updatedAt(location.getUpdatedAt())
                    .primarySitePublicId(primarySitePublicId)
                    .primarySiteName(location.getSite().getName())
                    .linkedSites(linkedSites)
                    .build();
        });
    }



    private LocationWithSitesDTO toLocationWithSitesDTO(Location loc) {
        List<LocationWithSitesDTO.SiteInfo> linkedSites = loc.getAssignedSites().stream()
                .filter(sla -> sla.getIsActive() && !sla.getIsDeleted())
                .map(sla -> LocationWithSitesDTO.SiteInfo.builder()
                        .sitePublicId(sla.getSite().getPublicId())
                        .siteName(sla.getSite().getName())
                        .build())
                .toList();

        return LocationWithSitesDTO.builder()
                .publicId(loc.getPublicId())
                .name(loc.getName())
                .description(loc.getDescription())
                .isActive(loc.getIsActive())
                .createdBy(loc.getCreatedBy())
                .createdAt(loc.getCreatedAt())
                .updatedBy(loc.getUpdatedBy())
                .updatedAt(loc.getUpdatedAt())
                .primarySitePublicId(loc.getSite().getPublicId())
                .primarySiteName(loc.getSite().getName())
                .linkedSites(linkedSites)
                .build();
    }


    @Transactional
    public LocationResponseDTO createLocation(String companyPublicId, String sitePublicId, LocationRequestDTO dto) {
        var user = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
        Site site = siteRepository.findByPublicIdAndCompanyAndIsDeletedFalse(sitePublicId, company)
                .orElseThrow(() -> new NotFoundException("Site not found: " + sitePublicId));

        boolean existsActive = locationRepository.existsByNameAndSiteAndCompanyAndIsDeletedFalse(dto.getName(), site, company);
        if (existsActive) {
            throw new IllegalArgumentException("Location name already exists in this site");
        }

        var deletedLocationOpt = locationRepository.findByNameAndSiteAndCompanyAndIsDeletedTrue(dto.getName(), site, company);
        if (deletedLocationOpt.isPresent()) {
            Location deletedLocation = deletedLocationOpt.get();
            deletedLocation.setIsDeleted(false);
            deletedLocation.setIsActive(dto.getIsActive() == null ? true : dto.getIsActive());
            deletedLocation.setDescription(dto.getDescription());
            deletedLocation.setUpdatedBy(user.getEmail());
            deletedLocation.setUpdatedAt(LocalDateTime.now());

            return toResponseDTO(locationRepository.save(deletedLocation));
        }

        Location location = Location.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive() == null ? true : dto.getIsActive())
                .isDeleted(false)
                .company(company)
                .site(site)
                .createdBy(user.getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        Location savedLocation = locationRepository.save(location);
        return toResponseDTO(savedLocation);
    }

    @Transactional
    public LocationResponseDTO updateLocation(String companyPublicId, String sitePublicId, String locationPublicId, LocationRequestDTO dto) {
        var user = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
        Site site = siteRepository.findByPublicIdAndCompanyAndIsDeletedFalse(sitePublicId, company)
                .orElseThrow(() -> new NotFoundException("Site not found: " + sitePublicId));
        Location location = locationRepository.findByPublicIdAndSiteAndCompanyAndIsDeletedFalse(locationPublicId, site, company)
                .orElseThrow(() -> new NotFoundException("Location not found: " + locationPublicId));

        location.setName(dto.getName());
        location.setDescription(dto.getDescription());
        location.setIsActive(dto.getIsActive() == null ? location.getIsActive() : dto.getIsActive());
        location.setUpdatedBy(user.getEmail());
        location.setUpdatedAt(LocalDateTime.now());

        Location updatedLocation = locationRepository.save(location);
        return toResponseDTO(updatedLocation);
    }

//    @Transactional
//    public void deleteLocation(String companyPublicId, String sitePublicId, String locationPublicId) {
//        var user = authUtils.getAuthenticatedUser()
//                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
//        Company company = companyRepository.findByPublicId(companyPublicId)
//                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
//        Site site = siteRepository.findByPublicIdAndCompanyAndIsDeletedFalse(sitePublicId, company)
//                .orElseThrow(() -> new NotFoundException("Site not found: " + sitePublicId));
//        Location location = locationRepository.findByPublicIdAndSiteAndCompanyAndIsDeletedFalse(locationPublicId, site, company)
//                .orElseThrow(() -> new NotFoundException("Location not found: " + locationPublicId));
//
//        location.setIsDeleted(true);
//        location.setIsActive(false);
//        location.setUpdatedBy(user.getEmail());
//        locationRepository.save(location);
//    }


    @Transactional
    public void deleteLocation(String companyPublicId, String sitePublicId, String locationPublicId) {
        var user = authUtils.getAuthenticatedUser()
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new NotFoundException("Company not found: " + companyPublicId));
        Site site = siteRepository.findByPublicIdAndCompanyAndIsDeletedFalse(sitePublicId, company)
                .orElseThrow(() -> new NotFoundException("Site not found: " + sitePublicId));
        Location location = locationRepository.findByPublicIdAndSiteAndCompanyAndIsDeletedFalse(locationPublicId, site, company)
                .orElseThrow(() -> new NotFoundException("Location not found: " + locationPublicId));

        // Soft delete all linked site assignments for this location
        List<SiteLocationAssignment> assignments = siteLocationAssignmentRepository
                .findByLocationAndCompanyAndIsDeletedFalse(location, company);

        for (SiteLocationAssignment assignment : assignments) {
            assignment.setIsDeleted(true);
            assignment.setIsActive(false);
            assignment.setUpdatedBy(user.getEmail());
            assignment.setUpdatedAt(LocalDateTime.now());
            siteLocationAssignmentRepository.save(assignment);
        }

        // Soft delete the location
        location.setIsDeleted(true);
        location.setIsActive(false);
        location.setUpdatedBy(user.getEmail());
        locationRepository.save(location);
    }



//    private LocationResponseDTO toResponseDTO(Location location) {
//        return LocationResponseDTO.builder()
//                .publicId(location.getPublicId())
//                .name(location.getName())
//                .description(location.getDescription())
//                .sitePublicId(location.getSite().getPublicId())
//                .siteName(location.getSite().getName())
//                .isActive(location.getIsActive())
//                .createdBy(location.getCreatedBy())
//                .createdAt(location.getCreatedAt())
//                .updatedBy(location.getUpdatedBy())
//                .updatedAt(location.getUpdatedAt())
//                .build();
//    }
//
//    private LocationResponseDTO toResponseDTO(Location location) {
//        // fetch related entities eagerly if possible or null-check them safely
//        String sitePublicId = (location.getSite() != null) ? location.getSite().getPublicId() : null;
//        String siteName = (location.getSite() != null) ? location.getSite().getName() : null;
//
//        return LocationResponseDTO.builder()
//                .publicId(location.getPublicId())
//                .name(location.getName())
//                .description(location.getDescription())
//                .sitePublicId(sitePublicId)
//                .siteName(siteName)
//                .isActive(location.getIsActive())
//                .createdBy(location.getCreatedBy())
//                .createdAt(location.getCreatedAt())
//                .updatedBy(location.getUpdatedBy())
//                .updatedAt(location.getUpdatedAt())
//                .build();
//    }

}



