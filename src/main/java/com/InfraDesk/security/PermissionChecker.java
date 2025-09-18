package com.InfraDesk.security;

import com.InfraDesk.entity.Membership;
import com.InfraDesk.enums.PermissionCode;
import com.InfraDesk.enums.Role;
import com.InfraDesk.repository.MembershipRepository;
import com.InfraDesk.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

//@Component("perm")
//@RequiredArgsConstructor
//public class PermissionChecker {
//    private final PermissionService permissionService;
//
//    public boolean check(String companyId, String permissionCode) {
//        var auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) return false;
//
//        if (cud.getRole() == Role.SUPER_ADMIN) return true;
//
//        Long userId = cud.getUserId();
//        return permissionService.hasPermission(userId, companyId, permissionCode);
//    }
//}


//@Component("perm")
//@RequiredArgsConstructor
//public class PermissionChecker {
//    private final PermissionService permissionService;
//
//    public boolean check(String companyId, PermissionCode code) {
//        var auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) return false;
//
//        if (cud.getRole() == Role.SUPER_ADMIN) return true;
//
//        return permissionService.hasPermission(cud.getUserId(), companyId, code.name());
//    }
//}


//@Component("perm")
//@RequiredArgsConstructor
//public class PermissionChecker {
//    private final PermissionService permissionService;
//
//    public boolean check(String companyId, PermissionCode code) {
//        var auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
//            return false;
//        }
//
//        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
//
//        if (cud.getRole() == Role.SUPER_ADMIN) {
//            return true;
//        }
//
//        // Keep PermissionCode typed (safer than String)
//        return permissionService.hasPermission(cud.getUserId(), companyId, code.name());
//    }
//}


@Component("perm")
@RequiredArgsConstructor
public class PermissionChecker {
    private final PermissionService permissionService;
    private final MembershipRepository membershipRepository; // Inject repository to fetch membership

    public boolean check(String companyId, PermissionCode code) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();

        // SUPER_ADMIN global bypass still via User.role
        if (cud.getRole() == Role.SUPER_ADMIN) {
            return true;
        }

        // Find membership for user + company
        Membership membership = membershipRepository
                .findByUser_IdAndCompany_PublicId(cud.getUserId(), companyId)
                .orElse(null);

        if (membership == null || !membership.getIsActive()) {
            return false; // No membership or inactive membership
        }

        // Use membership role to check permission
        return permissionService.hasPermission(
                cud.getUserId(),
                companyId,
                code.name()
//                membership.getRole() // pass membership role here (see next)
        );
    }
}




