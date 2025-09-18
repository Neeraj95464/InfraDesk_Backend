
package com.InfraDesk.controller;

import com.InfraDesk.dto.*;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.CompanyDomain;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // Company registration - often requires elevated permissions, example with 'CAN_MANAGE_USERS'
    @PostMapping("/register/company")
//    @PreAuthorize("@perm.check(null, 'CAN_CONFIGURE_COMPANY')") // No companyId yet, so pass null
    public ResponseEntity<ApiResponse<CompanyDTO>> createCompany(@RequestBody CompanyRegistrationRequest company) {
        CompanyDTO savedCompany = companyService.registerCompany(company);
        return ResponseEntity.ok(ApiResponse.success("Company registered successfully", savedCompany));
    }

    // Add extra domains to company - secured by permission on companyId
    @PostMapping("{companyId}/reserve-domains")
//    @PreAuthorize("@perm.check(#companyId.toString(), 'CAN_CONFIGURE_COMPANY')")
    @PreAuthorize("@perm.check(#companyId.toString(), T(com.InfraDesk.enums.PermissionCode).COMPANY_MANAGE)")
    public ResponseEntity<ApiResponse<List<String>>> addExtraDomains(
            @PathVariable Long companyId,
            @RequestBody AddDomainsRequest request) {

        try {
            List<CompanyDomain> saved = companyService.addExtraDomains(request.getDomains(), companyId);
            List<String> addedDomains = saved.stream()
                    .map(CompanyDomain::getDomain)
                    .toList();

            return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                    .success(true)
                    .message("Domains added successfully.")
                    .data(addedDomains)
                    .build());

        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.<List<String>>builder()
                    .success(false)
                    .message(ex.getMessage())
                    .data(null)
                    .build());
        }
    }

    // Get companies belonging to authenticated user
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginatedResponse<CompanyDTO>> getMyCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<CompanyDTO> response = companyService.getMyCompanies(page, size);
        return ResponseEntity.ok(response);
    }

    // Get company by id - secured with permission
    @GetMapping("/{companyId}")
//    @PreAuthorize("@perm.check(#companyId.toString(), 'CAN_CONFIGURE_COMPANY')")
    @PreAuthorize("@perm.check(#companyId.toString(), T(com.InfraDesk.enums.PermissionCode).COMPANY_VIEW)")
    public ResponseEntity<CompanyDTO> getCompanyById(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyService.getActiveCompanyById(companyId));
    }

    // Update company - requires permission check
    @PutMapping("/{companyId}")
//    @PreAuthorize("@perm.check(#companyId.toString(), 'CAN_CONFIGURE_COMPANY')")
    @PreAuthorize("@perm.check(#companyId.toString(), T(com.InfraDesk.enums.PermissionCode).COMPANY_MANAGE)")
    public ResponseEntity<String> updateCompany(@PathVariable Long companyId, @RequestBody Company updatedCompany) {
        companyService.updateCompany(companyId, updatedCompany);
        return ResponseEntity.ok("Company updated successfully");
    }

    // Soft delete company with permission check
    @DeleteMapping("/{companyId}")
//    @PreAuthorize("@perm.check(#companyId.toString(), 'CAN_CONFIGURE_COMPANY')")
    @PreAuthorize("@perm.check(#companyId.toString(), T(com.InfraDesk.enums.PermissionCode).COMPANY_CONFIGURE)")
    public ResponseEntity<String> softDeleteCompany(@PathVariable Long companyId) {
        companyService.deleteCompany(companyId);
        return ResponseEntity.ok("Company soft-deleted successfully");
    }
}
