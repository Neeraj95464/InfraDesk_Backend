package com.InfraDesk.controller;

import com.InfraDesk.dto.CompanySettingsDTO;
import com.InfraDesk.service.CompanySettingsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/settings")
@RequiredArgsConstructor
public class CompanySettingsController {

    private static final Logger log = LoggerFactory.getLogger(CompanySettingsController.class);
    private final CompanySettingsService settingsService;

    @PostMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<CompanySettingsDTO> saveSettings(
            @PathVariable String companyId,
            @RequestBody CompanySettingsDTO dto
    ) {
        return ResponseEntity.ok(settingsService.saveSettings(companyId, dto));
    }

    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_MANAGE')")
    public ResponseEntity<CompanySettingsDTO> getSettings(
            @PathVariable String companyId
    ) {
        return ResponseEntity.ok(settingsService.getSettings(companyId));
    }
}
