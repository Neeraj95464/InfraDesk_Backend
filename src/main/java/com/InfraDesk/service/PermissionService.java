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
