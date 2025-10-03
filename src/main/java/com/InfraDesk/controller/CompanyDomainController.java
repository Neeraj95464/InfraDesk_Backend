//package com.InfraDesk.controller;
//
//import com.InfraDesk.dto.AddDomainsRequest;
//import com.InfraDesk.dto.ApiResponse;
//import com.InfraDesk.dto.CompanyDomainDTO;
//import com.InfraDesk.entity.CompanyDomain;
//import com.InfraDesk.service.CompanyDomainService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/companies/domains")
//@RequiredArgsConstructor
//@Validated
//public class CompanyDomainController {
//
//    private final CompanyDomainService companyDomainService;
//
//    @PostMapping("{companyId}/reserve-domains")
//    @PreAuthorize("@perm.check(#companyId.toString(), 'CAN_CONFIGURE_COMPANY')")
//    public ResponseEntity<ApiResponse<List<String>>> addExtraDomains(
//            @PathVariable String companyId,
//            @RequestBody AddDomainsRequest request) {
//
//        List<CompanyDomainDTO> saved = companyDomainService.addExtraDomains(request.getDomains(), companyId);
//        List<String> addedDomains = saved.stream()
//                .map(CompanyDomain::getDomain)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
//                .success(true)
//                .message("Domains added successfully.")
//                .data(addedDomains)
//                .build());
//    }
//
//    @GetMapping("{companyId}/domains")
//    @PreAuthorize("@perm.check(#companyId.toString(), 'CAN_CONFIGURE_COMPANY')")
//    public ResponseEntity<ApiResponse<Page<CompanyDomain>>> getDomains(
//            @PathVariable String companyId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        Page<CompanyDomain> domainsPage = companyDomainService.getDomainsByCompany(companyId, PageRequest.of(page, size));
//        return ResponseEntity.ok(ApiResponse.<Page<CompanyDomain>>builder()
//                .success(true)
//                .message("Company domains retrieved")
//                .data(domainsPage)
//                .build());
//    }
//
//    @GetMapping("{companyId}/domains/{domainId}")
//    @PreAuthorize("@perm.check(#companyId.toString(), 'CAN_CONFIGURE_COMPANY')")
//    public ResponseEntity<ApiResponse<CompanyDomain>> getDomain(
//            @PathVariable String companyId,
//            @PathVariable String domainId) {
//
//        CompanyDomain domain = companyDomainService.getDomainById(companyId, domainId);
//        return ResponseEntity.ok(ApiResponse.<CompanyDomain>builder()
//                .success(true)
//                .message("Domain retrieved")
//                .data(domain)
//                .build());
//    }
//
//    @PutMapping("{companyId}/domains/{domainId}")
//    @PreAuthorize("@perm.check(#companyId.toString(), 'CAN_CONFIGURE_COMPANY')")
//    public ResponseEntity<ApiResponse<CompanyDomain>> updateDomain(
//            @PathVariable String companyId,
//            @PathVariable String domainId,
//            @RequestBody @Validated AddDomainsRequest request) {
//
//        // Expecting single domain update; adjust if multiple
//        if (request.getDomains() == null || request.getDomains().size() != 1) {
//            return ResponseEntity.badRequest().body(ApiResponse.<CompanyDomain>builder()
//                    .success(false)
//                    .message("Exactly one domain must be provided for update")
//                    .build());
//        }
//
//        CompanyDomain updated = companyDomainService.updateDomain(companyId, domainId, request.getDomains().get(0), true);
//        return ResponseEntity.ok(ApiResponse.<CompanyDomain>builder()
//                .success(true)
//                .message("Domain updated successfully")
//                .data(updated)
//                .build());
//    }
//
//    @DeleteMapping("{companyId}/domains/{domainId}")
//    @PreAuthorize("@perm.check(#companyId.toString(), 'CAN_CONFIGURE_COMPANY')")
//    public ResponseEntity<ApiResponse<Void>> deleteDomain(
//            @PathVariable String companyId,
//            @PathVariable String domainId) {
//
//        companyDomainService.deleteDomain(companyId, domainId);
//        return ResponseEntity.ok(ApiResponse.<Void>builder()
//                .success(true)
//                .message("Domain deleted (soft delete) successfully")
//                .build());
//    }
//}


package com.InfraDesk.controller;

import com.InfraDesk.dto.AddDomainsRequest;
import com.InfraDesk.dto.ApiResponse;
import com.InfraDesk.dto.CompanyDomainDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.service.CompanyDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies/domains")
@RequiredArgsConstructor
@Validated
public class CompanyDomainController {

    private final CompanyDomainService companyDomainService;

    @PostMapping("{companyId}/reserve-domains")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<ApiResponse<List<String>>> addExtraDomains(
            @PathVariable String companyId,
            @RequestBody AddDomainsRequest request) {
//        System.out.println("Request Received with "+companyId +" "+request);
        List<CompanyDomainDTO> saved = companyDomainService.addExtraDomains(request.getDomains(), companyId);

        // Map DTOs to domain strings
        List<String> addedDomains = saved.stream()
                .map(CompanyDomainDTO::getDomain)
                .toList();

        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .success(true)
                .message("Domains added successfully.")
                .data(addedDomains)
                .build());
    }

    @GetMapping("{companyId}/domains")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<ApiResponse<PaginatedResponse<CompanyDomainDTO>>> getDomains(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PaginatedResponse<CompanyDomainDTO> domainsPage = companyDomainService.getDomainsByCompany(companyId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.<PaginatedResponse<CompanyDomainDTO>>builder()
                .success(true)
                .message("Company domains retrieved")
                .data(domainsPage)
                .build());
    }

    @GetMapping("{companyId}/domains/{domainId}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<ApiResponse<CompanyDomainDTO>> getDomain(
            @PathVariable String companyId,
            @PathVariable String domainId) {

        CompanyDomainDTO domain = companyDomainService.getDomainById(companyId, domainId);
        return ResponseEntity.ok(ApiResponse.<CompanyDomainDTO>builder()
                .success(true)
                .message("Domain retrieved")
                .data(domain)
                .build());
    }

    @PutMapping("{companyId}/domains/{domainId}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<ApiResponse<CompanyDomainDTO>> updateDomain(
            @PathVariable String companyId,
            @PathVariable String domainId,
            @RequestBody @Validated AddDomainsRequest request) {

        if (request.getDomains() == null || request.getDomains().size() != 1) {
            return ResponseEntity.badRequest().body(ApiResponse.<CompanyDomainDTO>builder()
                    .success(false)
                    .message("Exactly one domain must be provided for update")
                    .build());
        }

        CompanyDomainDTO updated = companyDomainService.updateDomain(companyId, domainId, request.getDomains().get(0), true);
        return ResponseEntity.ok(ApiResponse.<CompanyDomainDTO>builder()
                .success(true)
                .message("Domain updated successfully")
                .data(updated)
                .build());
    }

    @DeleteMapping("{companyId}/domains/{domainId}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<ApiResponse<Void>> deleteDomain(
            @PathVariable String companyId,
            @PathVariable String domainId) {

        companyDomainService.deleteDomain(companyId, domainId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Domain deleted (soft delete) successfully")
                .build());
    }
}
