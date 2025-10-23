package com.InfraDesk.service;

import com.InfraDesk.audit.AuthenticationSuccessListener;
import com.InfraDesk.dto.MembershipFilterRequest;
import com.InfraDesk.dto.MembershipInfoDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.UserMembershipDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Membership;
import com.InfraDesk.enums.PermissionCode;
import com.InfraDesk.entity.User;
import com.InfraDesk.enums.Role;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.mapper.FilteredMembershipMapper;
import com.InfraDesk.mapper.MembershipMapper;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.MembershipRepository;
import com.InfraDesk.repository.UserLoginAuditRepository;
import com.InfraDesk.repository.UserRepository;
import com.InfraDesk.util.AuthUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.InfraDesk.specification.MembershipSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private static final Logger log = LoggerFactory.getLogger(MembershipService.class);
    private final MembershipRepository membershipRepository;
    private final UserLoginAuditRepository userLoginAuditRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AuthUtils authUtils;

    public PaginatedResponse<UserMembershipDTO> getUsersByCompanyWithMemberships(String companyId, Pageable pageable) {

        // Returns memberships for company excluding role USER, active and not deleted
//        Page<Membership> membershipsPage = membershipRepository
//                .findByCompany_PublicIdAndRoleIsNotAndIsActiveTrueAndIsDeletedFalse(companyId, Role.USER, pageable);

        List<Role> excludedRoles = Arrays.asList(Role.USER, Role.EXTERNAL_USER);
        Page<Membership> membershipsPage = membershipRepository.findByCompanyPublicIdAndRolesNotInAndIsActiveTrueAndIsDeletedFalse(companyId, excludedRoles, pageable);

        // Group memberships by user to build UserMembershipDTO
        Map<User, List<Membership>> membershipsByUser = membershipsPage.getContent().stream()
                .collect(Collectors.groupingBy(Membership::getUser));

        List<UserMembershipDTO> content = membershipsByUser.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<MembershipInfoDTO> membershipDTOs = entry.getValue().stream()
                            .map(m -> MembershipInfoDTO.builder()
                                    .companyPublicId(m.getCompany().getPublicId())
                                    .companyName(m.getCompany().getName())
                                    .role(m.getRole())
                                    .isActive(m.getIsActive())
                                    .createdAt(m.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList());

                    LocalDateTime lastLoginAt = userLoginAuditRepository.findLastLoginByUserId(user.getId());

                    return UserMembershipDTO.builder()
                            .userPublicId(user.getPublicId())
                            .email(user.getEmail())
                            .username(user.getEmployeeProfiles().isEmpty() ? null : user.getEmployeeProfiles().get(0).getName())
                            .isActive(user.getIsActive())
                            .createdAt(user.getCreatedAt())
                            .lastLoginAt(lastLoginAt)
                            .memberships(membershipDTOs)
                            .build();
                })
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                membershipsPage.getNumber(),
                membershipsPage.getSize(),
                membershipsPage.getTotalElements(),
                membershipsPage.getTotalPages(),
                membershipsPage.isLast()
        );
    }

    public PaginatedResponse<UserMembershipDTO> getAllUsersByCompanyWithMemberships(String companyId, Pageable pageable) {
        // Fetch paginated memberships for the company (no role/isActive/isDeleted filter)
        Page<Membership> membershipsPage = membershipRepository
                .findByCompany_PublicId(companyId, pageable);

        // Group memberships by user
        Map<User, List<Membership>> membershipsByUser = membershipsPage.getContent().stream()
                .collect(Collectors.groupingBy(Membership::getUser));

        List<UserMembershipDTO> content = membershipsByUser.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<MembershipInfoDTO> membershipDTOs = entry.getValue().stream()
                            .map(m -> MembershipInfoDTO.builder()
                                    .companyPublicId(m.getCompany().getPublicId())
                                    .companyName(m.getCompany().getName())
                                    .role(m.getRole())
                                    .isActive(m.getIsActive())
                                    .createdAt(m.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList());

                    LocalDateTime lastLoginAt = userLoginAuditRepository.findLastLoginByUserId(user.getId());

                    return UserMembershipDTO.builder()
                            .userPublicId(user.getPublicId())
                            .email(user.getEmail())
                            .username(user.getEmployeeProfiles().isEmpty() ? null : user.getEmployeeProfiles().get(0).getName())
                            .isActive(user.getIsActive())
                            .createdAt(user.getCreatedAt())
                            .lastLoginAt(lastLoginAt)
                            .memberships(membershipDTOs)
                            .build();
                })
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                membershipsPage.getNumber(),
                membershipsPage.getSize(),
                membershipsPage.getTotalElements(),
                membershipsPage.getTotalPages(),
                membershipsPage.isLast()
        );
    }


    @Transactional
    public MembershipInfoDTO assignMembership(String emailId, String companyId, Role role) {

        User user = userRepository.findByEmail(emailId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new BusinessException("COMPANY_NOT_FOUND", "Company not found"));

        // Find existing membership if any
        Optional<Membership> existingOpt = membershipRepository.findByUserAndCompany(user, company);

        if (existingOpt.isPresent()) {
            Membership existing = existingOpt.get();
            // If role is same, throw exception
            if (existing.getRole() == role) {
                throw new BusinessException("MEMBERSHIP_EXISTS", "This user already has this role in the company");
            }
            // Update role and other flags
            existing.setRole(role);
            existing.setIsActive(true);
            existing.setIsDeleted(false);
            // Update updatedAt and updatedBy via auditing if configured
            Membership membership= membershipRepository.save(existing);
            return MembershipMapper.toDTO(membership);
        }

        // Create new membership if none exists
        Membership membership = Membership.builder()
                .user(user)
                .company(company)
                .role(role)
                .isActive(true)
                .isDeleted(false)
                .build();

        return MembershipMapper.toDTO(membershipRepository.save(membership));
    }


    public PaginatedResponse<UserMembershipDTO> filterMemberships(MembershipFilterRequest req, String companyId) {
        AuthUtils.CurrentUserWithPermissions currentUser =
                authUtils.getCurrentUserWithPermissions(companyId)
                        .orElseThrow(() -> new BusinessException("Auth user not found"));

        Specification<Membership> spec = MembershipSpecification.filter(req, companyId);

//        log.info("Filtering memberships by companyId={}, roles={}, isActive={}, createdAfter={}",
//                companyId, req.getRoles(), req.getIsActive(), req.getCreatedAfter());

        var pageable = PageRequest.of(req.getPage(), req.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        // Fetch filtered memberships page
        Page<Membership> membershipsPage = membershipRepository.findAll(spec, pageable);

        // Group memberships by User entity
        Map<User, List<Membership>> membershipsByUser = membershipsPage.getContent().stream()
                .collect(Collectors.groupingBy(Membership::getUser));

        // Map grouped memberships to UserMembershipDTO
        List<UserMembershipDTO> content = membershipsByUser.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<MembershipInfoDTO> membershipDTOs = entry.getValue().stream()
                            .map(m -> MembershipInfoDTO.builder()
                                    .companyPublicId(m.getCompany().getPublicId())
                                    .companyName(m.getCompany().getName())
                                    .role(m.getRole())
                                    .isActive(m.getIsActive())
                                    .createdAt(m.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList());

                    LocalDateTime lastLoginAt = userLoginAuditRepository.findLastLoginByUserId(user.getId());

                    return UserMembershipDTO.builder()
                            .userPublicId(user.getPublicId())
                            .email(user.getEmail())
                            .username(user.getEmployeeProfiles() == null || user.getEmployeeProfiles().isEmpty()
                                    ? null : user.getEmployeeProfiles().get(0).getName())
                            .isActive(user.getIsActive())
                            .createdAt(user.getCreatedAt())
                            .lastLoginAt(lastLoginAt)
                            .memberships(membershipDTOs)
                            .build();
                })
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                membershipsPage.getNumber(),
                membershipsPage.getSize(),
                membershipsPage.getTotalElements(),
                membershipsPage.getTotalPages(),
                membershipsPage.isLast()
        );
    }



}
