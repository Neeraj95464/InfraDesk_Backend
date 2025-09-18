//package com.InfraDesk.service;
//
//import com.InfraDesk.entity.Company;
//import com.InfraDesk.entity.Group;
//import com.InfraDesk.entity.User;
//import com.InfraDesk.repository.CompanyRepository;
//import com.InfraDesk.repository.GroupRepository;
//import com.InfraDesk.repository.UserRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.time.Instant;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//@Service
//public class GroupService {
//
//    private final GroupRepository groupRepository;
//    private final CompanyRepository companyRepository;
//    private final UserRepository userRepository;
//
//    public GroupService(GroupRepository groupRepository,
//                        CompanyRepository companyRepository,
//                        UserRepository userRepository) {
//        this.groupRepository = groupRepository;
//        this.companyRepository = companyRepository;
//        this.userRepository = userRepository;
//    }
//
//    /**
//     * Fetch all active groups for a company
//     */
//    public List<Group> getAllGroups(String companyId) {
//        return groupRepository.findByCompanyPublicIdAndIsActiveTrue(companyId);
//    }
//
//    /**
//     * Get a group by id
//     */
//    public Optional<Group> getGroup(String companyId, Long groupId) {
//        return groupRepository.findByIdAndCompanyPublicId(groupId, companyId);
//    }
//
//    /**
//     * Create a new group
//     */
//    @Transactional
//    public Group createGroup(String companyId, String name, String description, Set<Long> userIds, String createdBy) {
//        Company company = companyRepository.findByPublicId(companyId)
//                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
//
//        if (!StringUtils.hasText(name)) {
//            throw new IllegalArgumentException("Group name is required");
//        }
//
//        if (groupRepository.existsByCompanyPublicIdAndNameIgnoreCase(companyId, name)) {
//            throw new IllegalArgumentException("Group with this name already exists for the company");
//        }
//
//        Set<User> users = new HashSet<>();
//        if (userIds != null && !userIds.isEmpty()) {
//            users.addAll(userRepository.findAllById(userIds));
//        }
//
//        Group group = Group.builder()
//                .company(company)
//                .name(StringUtils.capitalize(name.trim()))
//                .description(description)
//                .users(users)
//                .isActive(true)
//                .createdAt(Instant.now())
//                .createdBy(createdBy)
//                .build();
//
//        return groupRepository.save(group);
//    }
//
//    /**
//     * Update group details
//     */
//    @Transactional
//    public Group updateGroup(String companyId, Long groupId, String name, String description,
//                             Set<Long> userIds, Boolean isActive, String updatedBy) {
//        Group group = groupRepository.findByIdAndCompanyPublicId(groupId, companyId)
//                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
//
//        if (StringUtils.hasText(name)) {
//            group.setName(StringUtils.capitalize(name.trim()));
//        }
//
//        group.setDescription(description);
//
//        if (userIds != null) {
//            Set<User> users = new HashSet<>(userRepository.findAllById(userIds));
//            group.setUsers(users);
//        }
//
//        if (isActive != null) {
//            group.setActive(isActive);
//        }
//
//        group.setUpdatedAt(Instant.now());
//        group.setUpdatedBy(updatedBy);
//
//        return groupRepository.save(group);
//    }
//
//    /**
//     * Soft delete a group
//     */
//    @Transactional
//    public void deleteGroup(String companyId, Long groupId, String updatedBy) {
//        Group group = groupRepository.findByIdAndCompanyPublicId(groupId, companyId)
//                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
//        group.setActive(false);
//        group.setUpdatedAt(Instant.now());
//        group.setUpdatedBy(updatedBy);
//        groupRepository.save(group);
//    }
//}
//



package com.InfraDesk.service;

import com.InfraDesk.dto.GroupDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Group;
import com.InfraDesk.entity.User;
import com.InfraDesk.mapper.GroupMapper;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.GroupRepository;
import com.InfraDesk.repository.UserRepository;
import com.InfraDesk.util.AuthUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final AuthUtils authUtils;

    public GroupService(GroupRepository groupRepository,
                        CompanyRepository companyRepository,
                        UserRepository userRepository, AuthUtils authUtils) {
        this.groupRepository = groupRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.authUtils = authUtils;
    }

    /**
     * Fetch all active groups for a company
     */
    public List<GroupDTO> getAllGroups(String companyId) {
        List<Group> groups = groupRepository.findByCompanyPublicIdAndIsActiveTrue(companyId);
        return groups.stream().map(GroupMapper::toDTO).collect(Collectors.toList());
    }

    /**
     * Get a group by id
     */
    public GroupDTO getGroup(String companyId, Long groupId) {
        Group group = groupRepository.findByIdAndCompanyPublicId(groupId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        return GroupMapper.toDTO(group);
    }

    /**
     * Create a new group
     */
    @Transactional
    public GroupDTO createGroup(String companyId, GroupDTO dto) {
        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (!StringUtils.hasText(dto.getName())) {
            throw new IllegalArgumentException("Group name is required");
        }

        if (groupRepository.existsByCompanyPublicIdAndNameIgnoreCase(companyId, dto.getName())) {
            throw new IllegalArgumentException("Group with this name already exists for the company");
        }

        Set<User> users = new HashSet<>();
        if (dto.getUserIds() != null && !dto.getUserIds().isEmpty()) {
            users.addAll(userRepository.findAllById(dto.getUserIds()));
        }

        Group group = Group.builder()
                .company(company)
                .name(StringUtils.capitalize(dto.getName().trim()))
                .description(dto.getDescription())
                .users(users)
                .isActive(true)
                .createdAt(Instant.now())
                .createdBy(authUtils.getAuthenticatedEmail())
                .build();

        return GroupMapper.toDTO(groupRepository.save(group));
    }

    /**
     * Update group details
     */
    @Transactional
    public GroupDTO updateGroup(String companyId, Long groupId, GroupDTO dto) {
        Group group = groupRepository.findByIdAndCompanyPublicId(groupId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (StringUtils.hasText(dto.getName())) {
            group.setName(StringUtils.capitalize(dto.getName().trim()));
        }

        group.setDescription(dto.getDescription());

        if (dto.getUserIds() != null) {
            Set<User> users = new HashSet<>(userRepository.findAllById(dto.getUserIds()));
            group.setUsers(users);
        }

//        if (dto.isActive() != group.isActive()) {
//            group.setActive(dto.isActive());
//        }

        group.setUpdatedAt(Instant.now());
        group.setUpdatedBy(dto.getUpdatedBy());

        return GroupMapper.toDTO(groupRepository.save(group));
    }

    /**
     * Soft delete a group
     */
    @Transactional
    public void deleteGroup(String companyId, Long groupId, String updatedBy) {
        Group group = groupRepository.findByIdAndCompanyPublicId(groupId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        group.setActive(false);
        group.setUpdatedAt(Instant.now());
        group.setUpdatedBy(updatedBy);
        groupRepository.save(group);
    }
}
