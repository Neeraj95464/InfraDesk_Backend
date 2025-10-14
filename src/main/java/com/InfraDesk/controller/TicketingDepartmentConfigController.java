package com.InfraDesk.controller;

import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.TicketingDepartmentConfigCreateDTO;
import com.InfraDesk.dto.TicketingDepartmentConfigDTO;
import com.InfraDesk.entity.TicketingDepartmentConfig;
import com.InfraDesk.service.TicketingDepartmentConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/ticketing-dept-configs")
@RequiredArgsConstructor
public class TicketingDepartmentConfigController {

    private final TicketingDepartmentConfigService configService;

    // Get all configs with pagination and optional search; secured with TICKET_VIEW authority
    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<PaginatedResponse<TicketingDepartmentConfigDTO>> getConfigs(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        PaginatedResponse<TicketingDepartmentConfigDTO> response = configService.getConfigs(companyId, page, size, search);
        return ResponseEntity.ok(response);
    }

    // Get config by publicId
    @GetMapping("/{publicId}")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<TicketingDepartmentConfigDTO> getConfig(
            @PathVariable String companyId,
            @PathVariable String publicId) {

        TicketingDepartmentConfigDTO config = configService.getConfigByPublicId(publicId);
        return ResponseEntity.ok(config);
    }

    // Create config
    @PostMapping
    @PreAuthorize("@perm.check(#companyId, 'TICKET_MANAGE')")
    public ResponseEntity<TicketingDepartmentConfigDTO> createConfig(
            @PathVariable String companyId,
            @RequestBody TicketingDepartmentConfigCreateDTO config) {

//        System.out.println("config received "+config);
          config.setCompanyPublicId(companyId);

        TicketingDepartmentConfigDTO created = configService.createConfig(config);
        return ResponseEntity.ok(created);
    }

    // Update config
    @PutMapping("/{publicId}")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_MANAGE')")
    public ResponseEntity<TicketingDepartmentConfigDTO> updateConfig(
            @PathVariable String companyId,
            @PathVariable String publicId,
            @RequestBody TicketingDepartmentConfigCreateDTO updatedConfig) {
//        System.out.println("config received for update "+updatedConfig);
          updatedConfig.setCompanyPublicId(companyId);

        TicketingDepartmentConfigDTO updated = configService.updateConfig(publicId, updatedConfig);
        return ResponseEntity.ok(updated);
    }

    // Delete config (soft delete)
    @DeleteMapping("/{publicId}")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_MANAGE')")
    public ResponseEntity<Void> deleteConfig(
            @PathVariable String companyId,
            @PathVariable String publicId) {

        configService.deleteConfig(publicId);
        return ResponseEntity.noContent().build();
    }
}
