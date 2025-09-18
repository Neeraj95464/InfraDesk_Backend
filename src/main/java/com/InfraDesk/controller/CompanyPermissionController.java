package com.InfraDesk.controller;

import com.InfraDesk.dto.RolePermissionsDTO;
import com.InfraDesk.enums.Role;
import com.InfraDesk.service.PermissionService;
import com.InfraDesk.service.RolePermissionAdminService;
import com.InfraDesk.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/companies/permissions")
@RequiredArgsConstructor
public class CompanyPermissionController {

    private final RolePermissionAdminService adminService;
    private final PermissionService permissionService;

    // Only tenant admins (or super admin) can configure the matrix
    @PutMapping("/{companyId}/matrix")
    @PreAuthorize("@perm.check(#companyId.toString(), T(com.InfraDesk.enums.PermissionCode).COMPANY_MANAGE)")
    public void updateMatrix(@PathVariable String companyId, @RequestBody RolePermissionsDTO body) {
//        System.out.println("Request was "+companyId +" "+" rolePermissionDTO "+body);
        adminService.setCompanyRolePermissions(companyId, body);
    }

    // Anyone with access can read (you might restrict this too)
    @GetMapping("/{companyId}/matrix")
//    @PreAuthorize("@perm.check(#companyId, T(com.InfraDesk.enums.PermissionCode).COMPANY_VIEW)")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_VIEW')")
    public Map<Role, List<String>> getMatrix(@PathVariable String companyId) {
        return adminService.getCompanyRolePermissions(companyId);
    }

    @GetMapping("/{companyId}/me")
    public Set<String> myPermissions(@PathVariable String companyId) {
//        System.out.println("request received with "+companyId);
        return permissionService.getUserPermissionsInCompany(
                SecurityUtils.currentUserId(), companyId
        );
    }
}

