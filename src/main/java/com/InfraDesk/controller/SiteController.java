package com.InfraDesk.controller;

import com.InfraDesk.dto.SiteRequestDTO;
import com.InfraDesk.dto.SiteResponseDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/sites")
@RequiredArgsConstructor
@Validated
public class SiteController {

    private final SiteService siteService;

    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<PaginatedResponse<SiteResponseDTO>> getSites(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<SiteResponseDTO> sites = siteService.getSites(companyId, page, size);
        PaginatedResponse<SiteResponseDTO> response = new PaginatedResponse<>(
                sites.getContent(),
                sites.getNumber(),
                sites.getSize(),
                sites.getTotalElements(),
                sites.getTotalPages(),
                sites.isLast());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<SiteResponseDTO> createSite(
            @PathVariable String companyId,
            @RequestBody @Validated SiteRequestDTO dto
    ) {
        SiteResponseDTO created = siteService.createSite(companyId, dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{siteId}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<SiteResponseDTO> updateSite(
            @PathVariable String companyId,
            @PathVariable String siteId,
            @RequestBody @Validated SiteRequestDTO dto

    ) {
        SiteResponseDTO updated = siteService.updateSite(companyId, siteId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{siteId}")
//    @PreAuthorize("@perm.check(#companyId, 'CAN_MANAGE_SITES')")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<Void> deleteSite(
            @PathVariable String companyId,
            @PathVariable String siteId
    ) {
        siteService.deleteSite(companyId, siteId);
        return ResponseEntity.noContent().build();
    }
}
