//package com.InfraDesk.service;
//
//import com.InfraDesk.entity.*;
////import com.InfraDesk.entity.UserCompanyRole;
//import com.InfraDesk.enums.Role;
//import com.InfraDesk.exception.CompanyNotFoundException;
//import com.InfraDesk.repository.CompanyRepository;
//import com.InfraDesk.repository.MembershipRepository;
//import com.InfraDesk.repository.RolePermissionRepository;
//import com.InfraDesk.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class PermissionService {
//
//    private final UserRepository userRepository;
//    private final MembershipRepository membershipRepository;
//    private final RolePermissionRepository rolePermissionRepository;
//    private final CompanyRepository companyRepository;
//
//    /**
//     * Fetch user permissions inside a company by public companyId (UUID-style string).
//     */
////    public Set<String> getUserPermissionsInCompany(Long userId, String publicCompanyId) {
////        User user = userRepository.findById(userId).orElseThrow();
////
////        // Global super admin bypass
////        if (user.getRole() == Role.SUPER_ADMIN) {
////            return Set.of("*");
////        }
////
////        // Resolve company by its publicId (since frontend uses "COM-xxxxxx")
////        Company company = companyRepository.findByPublicId(publicCompanyId)
////                .orElseThrow(() -> new CompanyNotFoundException(publicCompanyId));
////
////        // Lookup membership (user ↔ company)
////        Membership membership = membershipRepository.findByUser_IdAndCompany_Id(userId, company.getId())
////                .orElseThrow(() -> new IllegalStateException("User has no role in this company"));
////
////        // Company admin = full access in that tenant
////        if (membership.getRole() == Role.COMPANY_CONFIGURE ) {
////            return Set.of("*");
////        }
////
////        // Look up allowed permissions for that role in this company
////        return rolePermissionRepository
////                .findByCompanyIdAndRoleAndAllowedTrue(company.getId(), membership.getRole())
////                .stream()
////                .map(RolePermission::getPermission)
////                .map(Permission::getCode)
////                .collect(Collectors.toSet());
////    }
////
//    /**
//     * Check if user has a specific permission in a company.
//     */
//    public boolean hasPermission(Long userId, String publicCompanyId, String permissionCode) {
//        Set<String> perms = getUserPermissionsInCompany(userId, publicCompanyId);
//        return perms.contains("*") || perms.contains(permissionCode);
//    }
//    public Set<String> getUserPermissionsInCompany(Long userId, String publicCompanyId) {
//        User user = userRepository.findById(userId).orElseThrow();
//
//        if (user.getRole() == Role.SUPER_ADMIN) {
//            return Set.of("*");
//        }
//
//        Company company = companyRepository.findByPublicId(publicCompanyId)
//                .orElseThrow(() -> new CompanyNotFoundException(publicCompanyId));
//
//        Company rootParent = getRootParentCompany(company);
//
//        // Check if user has PARENT_ADMIN membership on root parent company
//        boolean isParentAdmin = membershipRepository
//                .findByUser_IdAndCompany_Id(userId, rootParent.getId())
//                .map(Membership::getRole)
//                .map(role -> role == Role.PARENT_ADMIN)
//                .orElse(false);
//
//        if (isParentAdmin) {
//            return Set.of("*");
//        }
//
//        // Check membership for the actual company
//        Membership membership = membershipRepository.findByUser_IdAndCompany_Id(userId, company.getId())
//                .orElseThrow(() -> new IllegalStateException("User has no role in this company"));
//
//        if (membership.getRole() == Role.COMPANY_CONFIGURE) {
//            return Set.of("*");
//        }
//
//        // Return permissions mapped to role for the company
//        return rolePermissionRepository
//                .findByCompanyIdAndRoleAndAllowedTrue(company.getId(), membership.getRole())
//                .stream()
//                .map(RolePermission::getPermission)
//                .map(Permission::getCode)
//                .collect(Collectors.toSet());
//    }
//
//    /**
//     * Walks up the company parent hierarchy until the root parent (no parent).
//     */
//    private Company getRootParentCompany(Company company) {
//        Company current = company;
//        // Defensive check to avoid infinite loops if any config problems
//        while (current.getParentCompany() != null) {
//            current = companyRepository.findById(current.getParentCompany().getId())
//                    .orElseThrow(() -> new CompanyNotFoundException("Parent company missing"));
//        }
//        return current;
//    }
//
//
//
//}




package com.InfraDesk.service;

import com.InfraDesk.entity.*;
import com.InfraDesk.enums.Role;
import com.InfraDesk.exception.CompanyNotFoundException;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.MembershipRepository;
import com.InfraDesk.repository.RolePermissionRepository;
import com.InfraDesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//@Service
//@RequiredArgsConstructor
//public class PermissionService {
//
//    private final UserRepository userRepository;
//    private final MembershipRepository membershipRepository;
//    private final RolePermissionRepository rolePermissionRepository;
//    private final CompanyRepository companyRepository;
//
//    /**
//     * Define simple permission hierarchy.
//     * Example: COMPANY_MANAGE automatically includes COMPANY_VIEW.
//     */
//    private static final Map<String, Set<String>> PERMISSION_HIERARCHY = Map.of(
//            "COMPANY_MANAGE", Set.of("COMPANY_VIEW")
//    );
//
//    /**
//     * Check if user has a specific permission in a company.
//     * Supports SUPER_ADMIN, PARENT_ADMIN, and COMPANY_CONFIGURE bypass.
//     * Supports hierarchy (e.g., MANAGE → VIEW).
//     */
//    public boolean hasPermission(Long userId, String publicCompanyId, String permissionCode) {
//        Set<String> perms = getUserPermissionsInCompany(userId, publicCompanyId);
//
//        if (perms.contains("*")) {
//            return true; // full access
//        }
//
//        // Direct permission
//        if (perms.contains(permissionCode)) {
//            return true;
//        }
//
//        // Hierarchy check (e.g., MANAGE implies VIEW)
//        return perms.stream()
//                .anyMatch(granted -> PERMISSION_HIERARCHY
//                        .getOrDefault(granted, Set.of())
//                        .contains(permissionCode));
//    }
//
//    /**
//     * Fetch user permissions inside a company by public companyId (UUID-style string).
//     */
//    public Set<String> getUserPermissionsInCompany(Long userId, String publicCompanyId) {
//        User user = userRepository.findById(userId).orElseThrow();
//
//        // Global super admin bypass
//        if (user.getRole() == Role.SUPER_ADMIN) {
//            return Set.of("*");
//        }
//
//        // Resolve company
//        Company company = companyRepository.findByPublicId(publicCompanyId)
//                .orElseThrow(() -> new CompanyNotFoundException(publicCompanyId));
//
//        // Find root parent
//        Company rootParent = getRootParentCompany(company);
//
//        // Parent admin bypass at root
//        boolean isParentAdmin = membershipRepository
//                .findByUser_IdAndCompany_Id(userId, rootParent.getId())
//                .map(Membership::getRole)
//                .map(role -> role == Role.PARENT_ADMIN)
//                .orElse(false);
//
//        if (isParentAdmin) {
//            return Set.of("*");
//        }
//
//        // Company membership check
//        Membership membership = membershipRepository
//                .findByUser_IdAndCompany_Id(userId, company.getId())
//                .orElseThrow(() -> new IllegalStateException("User has no role in this company"));
//
//        // Company admin bypass
//        if (membership.getRole() == Role.COMPANY_CONFIGURE) {
//            return Set.of("*");
//        }
//
//        // Role-specific permissions for this company
////        return rolePermissionRepository
////                .findByCompanyIdAndRoleAndAllowedTrue(company.getId(), membership.getRole())
////                .stream()
////                .map(RolePermission::getPermission)
////                .map(Permission::getCode)
////                .collect(Collectors.toSet());
//
//        return  rolePermissionRepository
//                .findPermissionCodesByCompanyIdAndRole(company.getId(), membership.getRole())
//                .stream()
//                .map(Enum::name)                  // Convert PermissionCode enum to string like "COMPANY_VIEW"
//                .collect(Collectors.toSet());
//        // Set<String>
//
//    }
//
//    /**
//     * Walks up the company parent hierarchy until the root parent (no parent).
//     */
//    private Company getRootParentCompany(Company company) {
//        Company current = company;
//        while (current.getParentCompany() != null) {
//            current = current.getParentCompany();
//        }
//        return current;
//    }
//}



@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final CompanyRepository companyRepository;

    public boolean hasPermission(Long userId, String publicCompanyId, String permissionCode) {
        Set<String> permissions = getUserPermissionsInCompany(userId, publicCompanyId);

        if (permissions.contains("*")) { return true; }  // full access wildcard

        return permissions.contains(permissionCode);
    }

    public Set<String> getUserPermissionsInCompany(Long userId, String publicCompanyId) {
        User user = userRepository.findById(userId).orElseThrow();

        if (user.getRole() == Role.SUPER_ADMIN) {
            return Set.of("*");
        }

        Company company = companyRepository.findByPublicId(publicCompanyId)
                .orElseThrow(() -> new CompanyNotFoundException(publicCompanyId));

        Membership membership = membershipRepository.findByUser_IdAndCompany_Id(userId, company.getId())
                .orElseThrow(() -> new IllegalStateException("User has no role in company"));

        if (membership.getRole() == Role.COMPANY_ADMIN || membership.getRole() == Role.PARENT_ADMIN) {
            return Set.of("*");
        }

        return rolePermissionRepository.findPermissionCodesByCompanyIdAndRole(company.getId(), membership.getRole())
                .stream()
                .map(Enum::name)  // Convert PermissionCode to String
                .collect(Collectors.toSet());
    }
}
