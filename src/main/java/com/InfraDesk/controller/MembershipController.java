package com.InfraDesk.controller;

import com.InfraDesk.dto.*;
import com.InfraDesk.entity.Membership;
import com.InfraDesk.enums.Role;
import com.InfraDesk.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private static final Logger log = LoggerFactory.getLogger(MembershipController.class);
    private final MembershipService membershipService;

    @GetMapping("/company/{companyId}/users")
    @PreAuthorize("@perm.check(#companyId, 'EMPLOYEE_MANAGE')")
    public PaginatedResponse<UserMembershipDTO> getUsersByCompany(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return membershipService.getUsersByCompanyWithMemberships(companyId, PageRequest.of(page, size));
    }

    @GetMapping("/company/{companyId}/users/all")
    @PreAuthorize("@perm.check(#companyId, 'EMPLOYEE_VIEW')")
    public PaginatedResponse<UserMembershipDTO> getAllUsersByCompany(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return membershipService.getAllUsersByCompanyWithMemberships(companyId, PageRequest.of(page, size));
    }

    @PostMapping("/assign/{companyId}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<MembershipInfoDTO> assignMembership(
            @PathVariable String companyId,
            @Valid @RequestBody MembershipAssignRequest request) {

        MembershipInfoDTO membership = membershipService.assignMembership(
                request.getEmailId(),
                companyId,
                Role.valueOf(request.getRole())
        );

        return ResponseEntity.ok(membership);
    }

    @GetMapping("{companyId}/filter")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public PaginatedResponse<UserMembershipDTO> filterMemberships(
            @ModelAttribute MembershipFilterRequest req,
            @PathVariable String companyId
    ) {

        return membershipService.filterMemberships(req, companyId);
    }

}

