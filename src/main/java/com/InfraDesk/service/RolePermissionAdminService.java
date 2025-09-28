package com.InfraDesk.service;

import com.InfraDesk.dto.RolePermissionsDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Permission;
import com.InfraDesk.entity.RolePermission;
import com.InfraDesk.enums.Role;
import com.InfraDesk.enums.PermissionCode;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.PermissionRepository;
import com.InfraDesk.repository.RolePermissionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RolePermissionAdminService {

    private final CompanyRepository companyRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional
    public void setCompanyRolePermissions(String companyId, RolePermissionsDTO req) {
        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        // Clear everything first
        rolePermissionRepository.deleteAllByCompany_Id(company.getId());
        rolePermissionRepository.flush();

        List<RolePermission> newPermissions = new ArrayList<>();

        for (Map.Entry<Role, List<String>> entry : req.getRolePermissions().entrySet()) {
            Role role = entry.getKey();
            Set<String> distinctPerms = new HashSet<>(entry.getValue());

            for (String permCode : distinctPerms) {
                PermissionCode permissionCode = PermissionCode.valueOf(permCode);
                Permission permission = permissionRepository.findById(permissionCode)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + permCode));

                newPermissions.add(RolePermission.builder()
                        .company(company)
                        .role(role)
                        .permission(permission.getCode())
                        .allowed(true)
                        .build());
            }
        }

        rolePermissionRepository.saveAll(newPermissions);
    }


    public Map<Role, List<String>> getCompanyRolePermissions(String publicCompanyId) {
        List<RolePermission> all = rolePermissionRepository.findByCompanyPublicId(publicCompanyId);
        Map<Role, List<String>> map = new EnumMap<>(Role.class);

        // Initialize map with all roles except SUPER_ADMIN
        for (Role role : Role.values()) {
            if (role != Role.SUPER_ADMIN) {
                map.put(role, new ArrayList<>());
            }
        }

        // Assign permissions for roles that have them (excluding SUPER_ADMIN)
        for (RolePermission rp : all) {
            if (rp.getRole() != Role.SUPER_ADMIN) {
                map.get(rp.getRole()).add(rp.getPermission().name());
            }
        }

        return map;
    }

}



