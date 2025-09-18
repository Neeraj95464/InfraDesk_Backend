//package com.InfraDesk.service;
//
//import com.InfraDesk.dto.RolePermissionsDTO;
//import com.InfraDesk.entity.Company;
//import com.InfraDesk.entity.Permission;
//import com.InfraDesk.entity.RolePermission;
//import com.InfraDesk.enums.Role;
//import com.InfraDesk.repository.CompanyRepository;
//import com.InfraDesk.repository.PermissionRepository;
//import com.InfraDesk.repository.RolePermissionRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//public class RolePermissionAdminService {
//
//    private final CompanyRepository companyRepository;
//    private final PermissionRepository permissionRepository;
//    private final RolePermissionRepository rolePermissionRepository;
//
//    @Transactional
//    public void setCompanyRolePermissions(Long companyId, RolePermissionsDTO req) {
//        Company company = companyRepository.findById(companyId).orElseThrow();
//
//        // Delete existing matrix for this company (optional: soft reset)
//        // You could also diff and upsert for large tenants.
//        rolePermissionRepository.deleteAll(
//                rolePermissionRepository.findAll().stream()
//                        .filter(rp -> rp.getCompany().getId().equals(companyId))
//                        .toList()
//        );
//
//        // Insert new config
//        for (Map.Entry<Role, List<String>> e : req.getRolePermissions().entrySet()) {
//            Role role = e.getKey();
//            for (String code : e.getValue()) {
//                Permission p = permissionRepository.findById(code)
//                        .orElseThrow(() -> new IllegalArgumentException("Unknown permission: "+code));
//                RolePermission rp = RolePermission.builder()
//                        .company(company)
//                        .role(role)
//                        .permission(p)
//                        .allowed(true)
//                        .build();
//                rolePermissionRepository.save(rp);
//            }
//        }
//    }
//
//    public Map<Role, List<String>> getCompanyRolePermissions(String companyId) {
//        List<RolePermission> all = rolePermissionRepository.findByCompanyPublicId(companyId);
//        Map<Role, List<String>> map = new EnumMap<>(Role.class);
//        for (RolePermission rp : all) {
//            map.computeIfAbsent(rp.getRole(), k -> new ArrayList<>())
//                    .add(rp.getPermission().getCode());
//        }
//        return map;
//    }
//
//}
//


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

//@Service
//@RequiredArgsConstructor
//public class RolePermissionAdminService {
//
//    private final CompanyRepository companyRepository;
//    private final PermissionRepository permissionRepository;
//    private final RolePermissionRepository rolePermissionRepository;
//
//    /**
//     * Reset and set the role → permissions matrix for a company.
//     */
//    @Transactional
//    public void setCompanyRolePermissions(Long companyId, RolePermissionsDTO req) {
//        Company company = companyRepository.findById(companyId)
//                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
//
//        // Remove existing role-permissions for this company
//        rolePermissionRepository.deleteAll(
//                rolePermissionRepository.findByCompanyId(companyId)
//        );
//
//        // Insert new config
//        for (Map.Entry<Role, List<String>> e : req.getRolePermissions().entrySet()) {
//            Role role = e.getKey();
//            for (String code : e.getValue()) {
//                Permission permission = permissionRepository.findById(PermissionCode.valueOf(code))
//                        .orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + code));
//
//                RolePermission rp = RolePermission.builder()
//                        .company(company)
//                        .role(role)
//                        .permission(permission.getCode())
//                        .allowed(true)
//                        .build();
//
//                rolePermissionRepository.save(rp);
//            }
//        }
//    }
//
//    /**
//     * Return a map of role → list of permission codes for a company.
//     */
//    public Map<Role, List<String>> getCompanyRolePermissions(String publicCompanyId) {
//        List<RolePermission> all = rolePermissionRepository.findByCompanyPublicId(publicCompanyId);
//        Map<Role, List<String>> map = new EnumMap<>(Role.class);
//
//        for (RolePermission rp : all) {
//            map.computeIfAbsent(rp.getRole(), k -> new ArrayList<>())
//                    .add(rp.getPermission().toString()); // convert enum to String
//        }
//        return map;
//    }
//}


@Service
@RequiredArgsConstructor
public class RolePermissionAdminService {

    private final CompanyRepository companyRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

//    @Transactional
//    public void setCompanyRolePermissions(String companyId, RolePermissionsDTO req) {
//        Company company = companyRepository.findByPublicId(companyId)
//                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
//
//        // Delete all existing RolePermission for this company
//        rolePermissionRepository.deleteAllByCompany_Id(company.getId());
//
//        // Insert new
//        for (Map.Entry<Role, List<String>> entry : req.getRolePermissions().entrySet()) {
//            Role role = entry.getKey();
//            for (String permCode : entry.getValue()) {
//                PermissionCode permissionCode = PermissionCode.valueOf(permCode);
//                Permission permission = permissionRepository.findById(permissionCode)
//                        .orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + permCode));
//
//                RolePermission rp = RolePermission.builder()
//                        .company(company)
//                        .role(role)
//                        .permission(permission.getCode()) // PermissionCode enum here
//                        .allowed(true)
//                        .build();
//                rolePermissionRepository.save(rp);
//            }
//        }
//    }


    @Transactional
    public void setCompanyRolePermissions(String companyId, RolePermissionsDTO req) {
        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        rolePermissionRepository.deleteAllByCompany_Id(company.getId());
        rolePermissionRepository.flush(); // force flush to DB

        for (Map.Entry<Role, List<String>> entry : req.getRolePermissions().entrySet()) {
            Role role = entry.getKey();
            // Deduplicate permissions just in case
            Set<String> distinctPerms = new HashSet<>(entry.getValue());
            for (String permCode : distinctPerms) {
                PermissionCode permissionCode = PermissionCode.valueOf(permCode);
                Permission permission = permissionRepository.findById(permissionCode)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + permCode));

                RolePermission rp = RolePermission.builder()
                        .company(company)
                        .role(role)
                        .permission(permission.getCode())
                        .allowed(true)
                        .build();
                rolePermissionRepository.save(rp);
            }
        }
    }




//    public Map<Role, List<String>> getCompanyRolePermissions(String publicCompanyId) {
//        List<RolePermission> all = rolePermissionRepository.findByCompanyPublicId(publicCompanyId);
//        Map<Role, List<String>> map = new EnumMap<>(Role.class);
//
//        // Initialize map with all roles, empty permission lists
//        for (Role role : Role.values()) {
//            map.put(role, new ArrayList<>());
//        }
//
//        // Assign permissions for roles that have them
//        for (RolePermission rp : all) {
//            map.get(rp.getRole()).add(rp.getPermission().name());
//        }
//
//        return map;
//    }


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



