package com.InfraDesk.controller;

import com.InfraDesk.dto.LocationRequestDTO;
import com.InfraDesk.dto.LocationResponseDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/sites/{siteId}/locations")
@RequiredArgsConstructor
@Validated
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<PaginatedResponse<LocationResponseDTO>> getLocations(
            @PathVariable String companyId,
            @PathVariable String siteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<LocationResponseDTO> locations = locationService.getLocationsIncludingAssigned(companyId, siteId, page, size);
        PaginatedResponse<LocationResponseDTO> response = new PaginatedResponse<>(
                locations.getContent(),
                locations.getNumber(),
                locations.getSize(),
                locations.getTotalElements(),
                locations.getTotalPages(),
                locations.isLast());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<LocationResponseDTO> createLocation(
            @PathVariable String companyId,
            @PathVariable String siteId,
            @RequestBody @Validated LocationRequestDTO dto
    ) {
        LocationResponseDTO created = locationService.createLocation(companyId, siteId, dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{locationId}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<LocationResponseDTO> updateLocation(
            @PathVariable String companyId,
            @PathVariable String siteId,
            @PathVariable String locationId,
            @RequestBody @Validated LocationRequestDTO dto
    ) {
        LocationResponseDTO updated = locationService.updateLocation(companyId, siteId, locationId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{locationId}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable String companyId,
            @PathVariable String siteId,
            @PathVariable String locationId
    ) {
        locationService.deleteLocation(companyId, siteId, locationId);
        return ResponseEntity.noContent().build();
    }
}

