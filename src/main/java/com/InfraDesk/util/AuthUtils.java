//package com.InfraDesk.util;
//
//import com.InfraDesk.dto.CurrentUserDTO;
//import com.InfraDesk.entity.Employee;
//import com.InfraDesk.entity.User;
//import com.InfraDesk.repository.EmployeeRepository;
//import com.InfraDesk.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
//@Component
//@RequiredArgsConstructor
//public class AuthUtils {
//
//    private final UserRepository userRepository;
//    private final EmployeeRepository employeeRepository;
//
//
//    private Authentication getAuthentication() {
//        return SecurityContextHolder.getContext().getAuthentication();
//    }
//
//    public String getAuthenticatedEmail() {
//        Authentication auth = getAuthentication();
//        return (auth != null) ? auth.getName() : null;
//    }
//
//    public Optional<User> getAuthenticatedUser() {
//        String email = getAuthenticatedEmail();
//        if (email == null) return Optional.empty();
//
//        return userRepository.findByEmail(email)
//                .filter(User::getIsActive)
//                .filter(u -> !u.getIsDeleted());
//    }
//
//    public Optional<Employee> getAuthenticatedEmployee(Long companyId) {
//        return getAuthenticatedUser().flatMap(user ->
//                employeeRepository.findByUserIdAndCompanyId(user.getId(), companyId)
//        );
//    }
//
//    /**
//     * ✅ Builds CurrentUserDTO combining global and company-specific details
//     */
//    public Optional<CurrentUserDTO> getCurrentUser(Long companyId) {
//        return getAuthenticatedUser().map(user -> {
//            Optional<Employee> employeeOpt = employeeRepository.findByUserIdAndCompanyId(user.getId(), companyId);
//
//            if (employeeOpt.isPresent()) {
//                Employee emp = employeeOpt.get();
//                return new CurrentUserDTO(
//                        user.getId(),
//                        emp.getEmployeeId(),
//                        emp.getName() != null ? emp.getName() : user.getEmail(),
//                        user.getEmail(),
//                        emp.getCompany().getId(),
//                        null
//                );
//            } else {
//                return new CurrentUserDTO(
//                        user.getId(),
//                        null,
//                        user.getEmail(), // fallback as username
//                        user.getEmail(),
//                        null,
//                        null
//                );
//            }
//        });
//    }
//
//
//}
//
//




package com.InfraDesk.util;

import com.InfraDesk.dto.CurrentUserDTO;
import com.InfraDesk.entity.Employee;
import com.InfraDesk.entity.Membership;
import com.InfraDesk.entity.User;
import com.InfraDesk.enums.Role;
import com.InfraDesk.repository.EmployeeRepository;
import com.InfraDesk.repository.MembershipRepository;
import com.InfraDesk.repository.UserRepository;
import com.InfraDesk.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final MembershipRepository membershipRepository;
    private final PermissionService permissionService;

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getAuthenticatedEmail() {
        Authentication auth = getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }

    public Optional<User> getAuthenticatedUser() {
        String email = getAuthenticatedEmail();
        if (email == null) return Optional.empty();

        return userRepository.findByEmail(email)
                .filter(User::getIsActive)
                .filter(u -> !u.getIsDeleted());
    }

    public Optional<Employee> getAuthenticatedEmployee(Long companyId) {
        return getAuthenticatedUser().flatMap(user ->
                employeeRepository.findByUserIdAndCompanyId(user.getId(), companyId)
        );
    }

    /**
     * ✅ Builds CurrentUserDTO combining global and company-specific details
     */
    public Optional<CurrentUserDTO> getCurrentUser(Long companyId) {
        return getAuthenticatedUser().map(user -> {
            Optional<Employee> employeeOpt = employeeRepository.findByUserIdAndCompanyId(user.getId(), companyId);

            if (employeeOpt.isPresent()) {
                Employee emp = employeeOpt.get();
                return new CurrentUserDTO(
                        user.getId(),
                        emp.getEmployeeId(),
                        emp.getName() != null ? emp.getName() : user.getEmail(),
                        user.getEmail(),
                        emp.getCompany().getId(),
                        null
                );
            } else {
                return new CurrentUserDTO(
                        user.getId(),
                        null,
                        user.getEmail(), // fallback as username
                        user.getEmail(),
                        null,
                        null
                );
            }
        });
    }

    /**
     * ✅ Central method: Get logged-in user's role in a company
     */
    public Optional<Role> getUserRoleInCompany(Long userId, String publicCompanyId) {
        return membershipRepository.findByUser_IdAndCompany_PublicId(userId, publicCompanyId)
                .map(Membership::getRole);
    }

    /**
     * ✅ Central method: Get logged-in user's permissions in a company
     */
    public Set<String> getUserPermissionsInCompany(Long userId, String publicCompanyId) {
        return permissionService.getUserPermissionsInCompany(userId, publicCompanyId);
    }

    /**
     * ✅ Combines user, role and permissions into one object for easy usage
     */
    public Optional<CurrentUserWithPermissions> getCurrentUserWithPermissions(String publicCompanyId) {
        return getAuthenticatedUser().map(user -> {
            // Role in this company
            Role role = getUserRoleInCompany(user.getId(), publicCompanyId).orElse(null);

            // Effective permissions
            Set<String> permissions = getUserPermissionsInCompany(user.getId(), publicCompanyId);

            return new CurrentUserWithPermissions(user, role, permissions);
        });
    }

    /**
     * DTO holding user + role + permissions
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CurrentUserWithPermissions {
        private User user;
        private Role role;
        private Set<String> permissions;

        public boolean hasPermission(String code) {
            return permissions.contains("*") || permissions.contains(code);
        }
    }
}


