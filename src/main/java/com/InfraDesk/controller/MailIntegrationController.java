package com.InfraDesk.controller;

import com.InfraDesk.dto.MailIntegrationResponseDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.service.MailIntegrationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// MailIntegrationController.java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies/{companyId}/mail-configs")
public class MailIntegrationController {

    private static final Logger log = LoggerFactory.getLogger(MailIntegrationController.class);
    private final MailIntegrationService mailIntegrationService;

    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<PaginatedResponse<MailIntegrationResponseDTO>> getMailConfigs(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MailIntegrationResponseDTO> pageData = mailIntegrationService.getMailConfigs(companyId, page, size);

        PaginatedResponse<MailIntegrationResponseDTO> response = new PaginatedResponse<>(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.isLast()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<Void> updateStatusOrDelete(
            @PathVariable String companyId,
            @PathVariable String publicId,
            @RequestParam String action) {

        mailIntegrationService.updateMailIntegrationStatusOrDelete(companyId,publicId, action);
        return ResponseEntity.noContent().build();
    }

}

