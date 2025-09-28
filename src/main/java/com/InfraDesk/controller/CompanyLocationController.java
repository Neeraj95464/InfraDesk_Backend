package com.InfraDesk.controller;

import com.InfraDesk.dto.LocationResponseDTO;
import com.InfraDesk.dto.LocationWithSitesDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/locations")
@RequiredArgsConstructor
@Validated
public class CompanyLocationController {

    private final LocationService locationService;

    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<PaginatedResponse<LocationResponseDTO>> getCompanyLocations(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Page<LocationResponseDTO> locations = locationService.getLocationsForCompany(companyId, page, size, search);
        PaginatedResponse<LocationResponseDTO> response = new PaginatedResponse<>(
                locations.getContent(),
                locations.getNumber(),
                locations.getSize(),
                locations.getTotalElements(),
                locations.getTotalPages(),
                locations.isLast()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/multi-site")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<PaginatedResponse<LocationWithSitesDTO>> getMultiSiteLocations(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Page<LocationWithSitesDTO> locations = locationService.getMultiSiteLinkedLocations(companyId, page, size, search);
        PaginatedResponse<LocationWithSitesDTO> response = new PaginatedResponse<>(
                locations.getContent(),
                locations.getNumber(),
                locations.getSize(),
                locations.getTotalElements(),
                locations.getTotalPages(),
                locations.isLast()
        );
        return ResponseEntity.ok(response);
    }
}
